package rw.auca.clinicbook.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.clinicbook.dto.AvailabilityRequest;
import rw.auca.clinicbook.dto.AvailabilityResponse;
import rw.auca.clinicbook.dto.SlotResponse;
import rw.auca.clinicbook.entity.Appointment;
import rw.auca.clinicbook.entity.AppointmentStatus;
import rw.auca.clinicbook.entity.Availability;
import rw.auca.clinicbook.entity.Doctor;
import rw.auca.clinicbook.exception.ApiException;
import rw.auca.clinicbook.repository.AppointmentRepository;
import rw.auca.clinicbook.repository.AvailabilityRepository;
import rw.auca.clinicbook.repository.DoctorRepository;
import rw.auca.clinicbook.security.AuthUser;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    public static final int SLOT_MINUTES = 30;

    private final AvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> forDoctor(Long doctorId) {
        return availabilityRepository.findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctorId)
                .stream().map(AvailabilityResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> mine(AuthUser user) {
        return forDoctor(currentDoctor(user).getId());
    }

    @Transactional
    public AvailabilityResponse add(AuthUser user, AvailabilityRequest req) {
        Doctor doctor = currentDoctor(user);
        if (!req.startTime().isBefore(req.endTime())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Start time must be before end time");
        }
        boolean overlaps = availabilityRepository.findByDoctorIdAndDayOfWeek(doctor.getId(), req.dayOfWeek()).stream()
                .anyMatch(a -> a.getStartTime().isBefore(req.endTime()) && req.startTime().isBefore(a.getEndTime()));
        if (overlaps) {
            throw new ApiException(HttpStatus.CONFLICT, "This time overlaps with existing availability");
        }
        Availability saved = availabilityRepository.save(Availability.builder()
                .doctor(doctor).dayOfWeek(req.dayOfWeek()).startTime(req.startTime()).endTime(req.endTime()).build());
        return AvailabilityResponse.from(saved);
    }

    @Transactional
    public void delete(AuthUser user, Long availabilityId) {
        Doctor doctor = currentDoctor(user);
        Availability a = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> ApiException.notFound("Availability"));
        if (!a.getDoctor().getId().equals(doctor.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only remove your own availability");
        }
        availabilityRepository.delete(a);
    }

    /** Free 30-minute slots for one doctor on one date. */
    @Transactional(readOnly = true)
    public List<SlotResponse> freeSlots(Long doctorId, LocalDate date) {
        if (!doctorRepository.existsById(doctorId)) {
            throw ApiException.notFound("Doctor");
        }
        Set<LocalDateTime> taken = appointmentRepository
                .findByDoctorIdAndStartAtBetweenOrderByStartAt(doctorId, date.atStartOfDay(), date.plusDays(1).atStartOfDay())
                .stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .map(Appointment::getStartAt)
                .collect(Collectors.toSet());
        LocalDateTime now = LocalDateTime.now();
        List<SlotResponse> slots = new ArrayList<>();
        for (Availability a : availabilityRepository.findByDoctorIdAndDayOfWeek(doctorId, date.getDayOfWeek().getValue())) {
            for (LocalTime t = a.getStartTime(); !t.plusMinutes(SLOT_MINUTES).isAfter(a.getEndTime()); t = t.plusMinutes(SLOT_MINUTES)) {
                LocalDateTime start = date.atTime(t);
                if (start.isAfter(now) && !taken.contains(start)) {
                    slots.add(new SlotResponse(start, start.plusMinutes(SLOT_MINUTES)));
                }
                if (t.plusMinutes(SLOT_MINUTES).isBefore(t)) {
                    break; // guard against wrapping past midnight
                }
            }
        }
        slots.sort((x, y) -> x.startAt().compareTo(y.startAt()));
        return slots;
    }

    /** True if the start time falls on a 30-minute grid inside the doctor's working hours. */
    public boolean isBookable(Long doctorId, LocalDateTime start) {
        LocalTime time = start.toLocalTime();
        LocalTime end = time.plusMinutes(SLOT_MINUTES);
        return availabilityRepository.findByDoctorIdAndDayOfWeek(doctorId, start.getDayOfWeek().getValue()).stream()
                .anyMatch(a -> !time.isBefore(a.getStartTime())
                        && !end.isAfter(a.getEndTime())
                        && end.isAfter(time)
                        && Duration.between(a.getStartTime(), time).toMinutes() % SLOT_MINUTES == 0);
    }

    private Doctor currentDoctor(AuthUser user) {
        return doctorRepository.findByUserId(user.id())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "No doctor profile for this account"));
    }
}
