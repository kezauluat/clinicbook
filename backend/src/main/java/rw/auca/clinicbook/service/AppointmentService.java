package rw.auca.clinicbook.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.clinicbook.document.VisitNote;
import rw.auca.clinicbook.dto.*;
import rw.auca.clinicbook.entity.*;
import rw.auca.clinicbook.exception.ApiException;
import rw.auca.clinicbook.messaging.AppointmentEvent;
import rw.auca.clinicbook.repository.AppointmentRepository;
import rw.auca.clinicbook.repository.DoctorRepository;
import rw.auca.clinicbook.repository.PatientRepository;
import rw.auca.clinicbook.repository.mongo.VisitNoteRepository;
import rw.auca.clinicbook.security.AuthUser;
import rw.auca.clinicbook.security.SecurityUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final long CANCEL_LIMIT_HOURS = 2;

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AvailabilityService availabilityService;
    private final VisitNoteRepository visitNoteRepository;
    private final ApplicationEventPublisher events;
    private final AuditService auditService;

    @Transactional
    public AppointmentResponse book(AuthUser user, BookAppointmentRequest req) {
        Patient patient;
        if (SecurityUtils.hasRole("RECEPTIONIST")) {
            if (req.patientId() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "patientId is required when booking for a patient");
            }
            patient = patientRepository.findById(req.patientId()).orElseThrow(() -> ApiException.notFound("Patient"));
        } else {
            patient = patientRepository.findByUserId(user.id())
                    .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "No patient profile for this account"));
        }
        Doctor doctor = doctorRepository.findById(req.doctorId()).orElseThrow(() -> ApiException.notFound("Doctor"));
        LocalDateTime start = normalize(req.startAt());
        checkSlot(doctor.getId(), start);

        Appointment appointment = saveOrConflict(Appointment.builder()
                .patient(patient).doctor(doctor)
                .startAt(start).endAt(start.plusMinutes(AvailabilityService.SLOT_MINUTES))
                .reason(req.reason())
                .build());

        events.publishEvent(AppointmentEvent.of("BOOKED", appointment));
        auditService.log(user.id(), "BOOK_APPOINTMENT", "Appointment", appointment.getId());
        return AppointmentResponse.from(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> myAppointments(AuthUser user) {
        Patient patient = patientRepository.findByUserId(user.id())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "No patient profile for this account"));
        return appointmentRepository.findByPatientIdOrderByStartAtDesc(patient.getId())
                .stream().map(AppointmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> forDay(LocalDate date) {
        return appointmentRepository.findByStartAtBetweenOrderByStartAt(date.atStartOfDay(), date.plusDays(1).atStartOfDay())
                .stream().map(AppointmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> doctorSchedule(AuthUser user, LocalDate date) {
        Doctor doctor = currentDoctor(user);
        return appointmentRepository.findByDoctorIdAndStartAtBetweenOrderByStartAt(
                        doctor.getId(), date.atStartOfDay(), date.plusDays(1).atStartOfDay())
                .stream().map(AppointmentResponse::from).toList();
    }

    @Transactional
    public AppointmentResponse cancel(AuthUser user, Long id) {
        Appointment a = find(id);
        if (!isStaff()) {
            ensureOwner(a, user);
            if (a.getStartAt().isBefore(LocalDateTime.now().plusHours(CANCEL_LIMIT_HOURS))) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Appointments can only be cancelled at least " + CANCEL_LIMIT_HOURS + " hours before the start time");
            }
        }
        requireStatus(a, AppointmentStatus.BOOKED, "Only booked appointments can be cancelled");
        a.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.saveAndFlush(a);
        events.publishEvent(AppointmentEvent.of("CANCELLED", a));
        auditService.log(user.id(), "CANCEL_APPOINTMENT", "Appointment", a.getId());
        return AppointmentResponse.from(a);
    }

    @Transactional
    public AppointmentResponse reschedule(AuthUser user, Long id, RescheduleRequest req) {
        Appointment a = find(id);
        if (!isStaff()) {
            ensureOwner(a, user);
        }
        requireStatus(a, AppointmentStatus.BOOKED, "Only booked appointments can be rescheduled");
        LocalDateTime start = normalize(req.startAt());
        checkSlot(a.getDoctor().getId(), start);
        a.setStartAt(start);
        a.setEndAt(start.plusMinutes(AvailabilityService.SLOT_MINUTES));
        a = saveOrConflict(a);
        events.publishEvent(AppointmentEvent.of("RESCHEDULED", a));
        auditService.log(user.id(), "RESCHEDULE_APPOINTMENT", "Appointment", a.getId());
        return AppointmentResponse.from(a);
    }

    @Transactional
    public AppointmentResponse checkIn(AuthUser user, Long id) {
        Appointment a = find(id);
        requireStatus(a, AppointmentStatus.BOOKED, "Only booked appointments can be checked in");
        a.setStatus(AppointmentStatus.CHECKED_IN);
        appointmentRepository.saveAndFlush(a);
        auditService.log(user.id(), "CHECK_IN", "Appointment", a.getId());
        return AppointmentResponse.from(a);
    }

    /** Doctor writes visit notes (MongoDB) and the appointment becomes COMPLETED. */
    @Transactional
    public VisitNoteResponse saveNote(AuthUser user, Long id, VisitNoteRequest req) {
        Doctor doctor = currentDoctor(user);
        Appointment a = find(id);
        if (!a.getDoctor().getId().equals(doctor.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This is not your appointment");
        }
        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot add notes to a cancelled appointment");
        }
        VisitNote note = visitNoteRepository.findByAppointmentId(a.getId()).orElseGet(() -> VisitNote.builder()
                .appointmentId(a.getId()).patientId(a.getPatient().getId()).doctorId(doctor.getId()).build());
        note.setNotes(req.notes());
        note.setPrescriptions(req.prescriptions() == null ? List.of() : req.prescriptions());
        note = visitNoteRepository.save(note);
        if (a.getStatus() != AppointmentStatus.COMPLETED) {
            a.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.saveAndFlush(a);
        }
        auditService.log(user.id(), "SAVE_VISIT_NOTE", "Appointment", a.getId());
        return VisitNoteResponse.from(note);
    }

    @Transactional(readOnly = true)
    public VisitNoteResponse getNote(AuthUser user, Long id) {
        Appointment a = find(id);
        boolean owner = a.getPatient().getUser().getId().equals(user.id());
        boolean assignedDoctor = a.getDoctor().getUser().getId().equals(user.id());
        if (!owner && !assignedDoctor && !SecurityUtils.hasRole("ADMIN")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot view this visit note");
        }
        return visitNoteRepository.findByAppointmentId(id).map(VisitNoteResponse::from)
                .orElseThrow(() -> ApiException.notFound("Visit note"));
    }

    // ---------- helpers ----------

    private void checkSlot(Long doctorId, LocalDateTime start) {
        if (!start.isAfter(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot book a time in the past");
        }
        if (!availabilityService.isBookable(doctorId, start)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "The doctor is not available at this time");
        }
        if (appointmentRepository.existsByDoctorIdAndStartAtAndStatusNot(doctorId, start, AppointmentStatus.CANCELLED)) {
            throw new ApiException(HttpStatus.CONFLICT, "Slot no longer available");
        }
    }

    /** The partial unique index catches two users booking the same slot at the same moment. */
    private Appointment saveOrConflict(Appointment a) {
        try {
            return appointmentRepository.saveAndFlush(a);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(HttpStatus.CONFLICT, "Slot no longer available");
        }
    }

    private Appointment find(Long id) {
        return appointmentRepository.findById(id).orElseThrow(() -> ApiException.notFound("Appointment"));
    }

    private Doctor currentDoctor(AuthUser user) {
        return doctorRepository.findByUserId(user.id())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "No doctor profile for this account"));
    }

    private void ensureOwner(Appointment a, AuthUser user) {
        if (!a.getPatient().getUser().getId().equals(user.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This is not your appointment");
        }
    }

    private void requireStatus(Appointment a, AppointmentStatus status, String message) {
        if (a.getStatus() != status) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private boolean isStaff() {
        return SecurityUtils.hasRole("RECEPTIONIST") || SecurityUtils.hasRole("ADMIN");
    }

    private static LocalDateTime normalize(LocalDateTime t) {
        return t.withSecond(0).withNano(0);
    }
}
