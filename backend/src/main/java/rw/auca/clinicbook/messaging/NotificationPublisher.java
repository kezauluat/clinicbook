package rw.auca.clinicbook.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import rw.auca.clinicbook.config.RabbitConfig;

/** Sends the event to RabbitMQ only AFTER the database transaction commits. */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAppointmentEvent(AppointmentEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE,
                    "appointment." + event.type().toLowerCase(), event);
        } catch (AmqpException e) {
            log.error("Could not publish {} for appointment {}: {}", event.type(), event.appointmentId(), e.getMessage());
        }
    }
}
