package rw.auca.clinicbook.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import rw.auca.clinicbook.dto.AppointmentResponse;
import rw.auca.clinicbook.dto.BookAppointmentRequest;
import rw.auca.clinicbook.entity.*;
import rw.auca.clinicbook.exception.ApiException;
import rw.auca.clinicbook.messaging.AppointmentEvent;
import rw.auca.clinicbook.repository.AppointmentRepository;
import rw.auca.clinicbook.repository.DoctorRepository;
import rw.auca.clinicbook.repository.PatientRepository;
import rw.auca.clinicbook.repository.mongo.VisitNoteRepository;
import rw.auca.clinicbook.security.AuthUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock PatientRepository patientRepository;
    @Mock DoctorRepository doctorRepository;
    @Mock AvailabilityService availabilityService;
    @Mock VisitNoteRepository visitNoteRepository;
    @Mock ApplicationEventPublisher events;
    @Mock AuditService auditService;
    @InjectMocks AppointmentService service;

    private final AuthUser patientUser = new AuthUser(10L, "patient@test.rw");
    private Patient patient;
    private Doctor doctor;
    private LocalDateTime start;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        patient = Patient.builder().id(1L)
                .user(User.builder().id(10L).fullName("Test Patient").email("patient@test.rw").build()).build();
        Department department = Department.builder().id(1L).name("General Medicine")
                .clinic(Clinic.builder().id(1L).name("Kigali Family Clinic").build()).build();
        doctor = Doctor.builder().id(2L).licenseNo("RMDC-1").department(department)
                .user(User.builder().id(20L).fullName("Dr. Test").email("doctor@test.rw").build()).build();
        start = LocalDateTime.now().plusDays(3).withHour(9).withMinute(0).withSecond(0).withNano(0);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Appointment appointmentAt(LocalDateTime time) {
        return Appointment.builder().id(5L).patient(patient).doctor(doctor)
                .startAt(time).endAt(time.plusMinutes(30)).status(AppointmentStatus.BOOKED).build();
    }

    @Test
    void booksFreeSlotAndPublishesEvent() {
        when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(availabilityService.isBookable(2L, start)).thenReturn(true);
        when(appointmentRepository.existsByDoctorIdAndStartAtAndStatusNot(2L, start, AppointmentStatus.CANCELLED)).thenReturn(false);
        when(appointmentRepository.saveAndFlush(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(100L);
            return a;
        });

        AppointmentResponse result = service.book(patientUser, new BookAppointmentRequest(2L, start, "Headache", null));

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.status()).isEqualTo("BOOKED");
        assertThat(result.endAt()).isEqualTo(start.plusMinutes(30));
        ArgumentCaptor<AppointmentEvent> event = ArgumentCaptor.forClass(AppointmentEvent.class);
        verify(events).publishEvent(event.capture());
        assertThat(event.getValue().type()).isEqualTo("BOOKED");
        assertThat(event.getValue().patientEmail()).isEqualTo("patient@test.rw");
    }

    @Test
    void takenSlotIsRejectedWith409() {
        when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(availabilityService.isBookable(2L, start)).thenReturn(true);
        when(appointmentRepository.existsByDoctorIdAndStartAtAndStatusNot(2L, start, AppointmentStatus.CANCELLED)).thenReturn(true);

        assertThatThrownBy(() -> service.book(patientUser, new BookAppointmentRequest(2L, start, null, null)))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.CONFLICT);
        verify(appointmentRepository, never()).saveAndFlush(any());
    }

    @Test
    void timeOutsideWorkingHoursIsRejectedWith400() {
        when(patientRepository.findByUserId(10L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(availabilityService.isBookable(2L, start)).thenReturn(false);

        assertThatThrownBy(() -> service.book(patientUser, new BookAppointmentRequest(2L, start, null, null)))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void patientCannotCancelLessThanTwoHoursBefore() {
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointmentAt(LocalDateTime.now().plusMinutes(60))));

        assertThatThrownBy(() -> service.cancel(patientUser, 5L))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void patientCannotCancelSomeoneElsesAppointment() {
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointmentAt(start)));

        assertThatThrownBy(() -> service.cancel(new AuthUser(99L, "other@test.rw"), 5L))
                .isInstanceOf(ApiException.class)
                .extracting("status").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void receptionistCanCancelEvenCloseToStartTime() {
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                new AuthUser(30L, "reception@test.rw"), null, List.of(new SimpleGrantedAuthority("ROLE_RECEPTIONIST"))));
        when(appointmentRepository.findById(5L)).thenReturn(Optional.of(appointmentAt(LocalDateTime.now().plusMinutes(30))));

        AppointmentResponse result = service.cancel(new AuthUser(30L, "reception@test.rw"), 5L);

        assertThat(result.status()).isEqualTo("CANCELLED");
        verify(events).publishEvent(any(AppointmentEvent.class));
    }
}
