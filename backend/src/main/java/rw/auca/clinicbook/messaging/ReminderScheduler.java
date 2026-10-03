package rw.auca.clinicbook.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rw.auca.clinicbook.entity.Appointment;
import rw.auca.clinicbook.entity.AppointmentStatus;
import rw.auca.clinicbook.repository.AppointmentRepository;

import java.time.LocalDateTime;
import java.util.List;

/** Every hour, send reminders for appointments starting 23-24 hours from now. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final ApplicationEventPublisher events;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional(readOnly = true)
    public void sendReminders() {
        LocalDateTime from = LocalDateTime.now().plusHours(23);
        List<Appointment> due = appointmentRepository.findByStatusAndStartAtBetween(AppointmentStatus.BOOKED, from, from.plusHours(1));
        due.forEach(a -> events.publishEvent(AppointmentEvent.of("REMINDER", a)));
        if (!due.isEmpty()) {
            log.info("Queued {} appointment reminders", due.size());
        }
    }
}
