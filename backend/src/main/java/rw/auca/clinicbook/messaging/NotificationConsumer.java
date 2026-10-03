package rw.auca.clinicbook.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import rw.auca.clinicbook.config.RabbitConfig;
import rw.auca.clinicbook.document.NotificationLog;
import rw.auca.clinicbook.repository.mongo.NotificationLogRepository;

import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("EEE dd MMM yyyy 'at' HH:mm");

    private final JavaMailSender mailSender;
    private final NotificationLogRepository notificationLogRepository;

    @RabbitListener(queues = RabbitConfig.EMAIL_QUEUE)
    public void sendEmail(AppointmentEvent event) {
        if (event.patientEmail() == null) {
            return;
        }
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom("ClinicBook <no-reply@clinicbook.rw>");
            mail.setTo(event.patientEmail());
            mail.setSubject(subject(event));
            mail.setText(body(event));
            mailSender.send(mail);
            saveLog(event, "EMAIL", event.patientEmail(), "SENT");
        } catch (Exception e) {
            saveLog(event, "EMAIL", event.patientEmail(), "FAILED");
            throw new IllegalStateException("Email failed, will retry", e); // retried, then dead-lettered
        }
    }

    /** Simulated SMS gateway: a real one (e.g. Africa's Talking) needs a paid API key. */
    @RabbitListener(queues = RabbitConfig.SMS_QUEUE)
    public void sendSms(AppointmentEvent event) {
        if (event.patientPhone() == null) {
            return;
        }
        String text = "ClinicBook: " + subject(event) + " with " + event.doctorName() + " on " + event.startAt().format(FMT);
        log.info("SMS to {}: {}", event.patientPhone(), text);
        saveLog(event, "SMS", event.patientPhone(), "SENT");
    }

    private String subject(AppointmentEvent e) {
        return switch (e.type()) {
            case "BOOKED" -> "Appointment confirmed";
            case "CANCELLED" -> "Appointment cancelled";
            case "RESCHEDULED" -> "Appointment rescheduled";
            case "REMINDER" -> "Reminder: appointment tomorrow";
            default -> "Appointment update";
        };
    }

    private String body(AppointmentEvent e) {
        return "Hello " + e.patientName() + ",\n\n"
                + subject(e) + ".\n"
                + "Doctor: " + e.doctorName() + " (" + e.departmentName() + ")\n"
                + "Date: " + e.startAt().format(FMT) + "\n"
                + "Reference: #" + e.appointmentId() + "\n\n"
                + "ClinicBook";
    }

    private void saveLog(AppointmentEvent e, String channel, String recipient, String status) {
        try {
            notificationLogRepository.save(NotificationLog.builder()
                    .eventType(e.type()).channel(channel).recipient(recipient)
                    .payload(subject(e) + " #" + e.appointmentId())
                    .status(status).attempts(1).build());
        } catch (Exception ex) {
            log.warn("Could not save notification log: {}", ex.getMessage());
        }
    }
}
