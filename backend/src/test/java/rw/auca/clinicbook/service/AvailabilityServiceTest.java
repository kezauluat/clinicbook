package rw.auca.clinicbook.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.auca.clinicbook.dto.SlotResponse;
import rw.auca.clinicbook.entity.Appointment;
import rw.auca.clinicbook.entity.AppointmentStatus;
import rw.auca.clinicbook.entity.Availability;
import rw.auca.clinicbook.repository.AppointmentRepository;
import rw.auca.clinicbook.repository.AvailabilityRepository;
import rw.auca.clinicbook.repository.DoctorRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock AvailabilityRepository availabilityRepository;
    @Mock DoctorRepository doctorRepository;
    @Mock AppointmentRepository appointmentRepository;
    @InjectMocks AvailabilityService service;

    private final LocalDate monday = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.next(DayOfWeek.MONDAY));

    private Availability morning() {
        return Availability.builder().id(1L).dayOfWeek(1)
                .startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(12, 0)).build();
    }

    @Test
    void slotInsideWorkingHoursIsBookable() {
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(1L, 1)).thenReturn(List.of(morning()));
        assertThat(service.isBookable(1L, monday.atTime(9, 0))).isTrue();
    }

    @Test
    void slotOutsideWorkingHoursIsNotBookable() {
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(1L, 1)).thenReturn(List.of(morning()));
        assertThat(service.isBookable(1L, monday.atTime(13, 0))).isFalse();
    }

    @Test
    void slotOffTheThirtyMinuteGridIsNotBookable() {
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(1L, 1)).thenReturn(List.of(morning()));
        assertThat(service.isBookable(1L, monday.atTime(9, 15))).isFalse();
    }

    @Test
    void slotEndingAfterClosingTimeIsNotBookable() {
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(1L, 1)).thenReturn(List.of(morning()));
        assertThat(service.isBookable(1L, monday.atTime(12, 0))).isFalse();
    }

    @Test
    void freeSlotsHideBookedSlotsButShowCancelledOnes() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        Appointment booked = Appointment.builder().startAt(monday.atTime(8, 0)).status(AppointmentStatus.BOOKED).build();
        Appointment cancelled = Appointment.builder().startAt(monday.atTime(8, 30)).status(AppointmentStatus.CANCELLED).build();
        when(appointmentRepository.findByDoctorIdAndStartAtBetweenOrderByStartAt(eq(1L), any(), any()))
                .thenReturn(List.of(booked, cancelled));
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(1L, 1)).thenReturn(List.of(morning()));

        List<SlotResponse> slots = service.freeSlots(1L, monday);

        assertThat(slots).hasSize(7); // 08:00-12:00 = 8 slots, minus the booked 08:00
        assertThat(slots.get(0).startAt()).isEqualTo(monday.atTime(8, 30));
    }
}
