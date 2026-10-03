package rw.auca.clinicbook.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("notification_logs")
@CompoundIndex(name = "status_sentAt", def = "{'status': 1, 'sentAt': -1}")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationLog {

    @Id
    private String id;

    private String eventType;   // APPOINTMENT_BOOKED, APPOINTMENT_CANCELLED, REMINDER
    private String channel;     // EMAIL or SMS
    private String recipient;
    private String payload;
    private String status;      // SENT or FAILED
    private int attempts;

    @Builder.Default
    private Instant sentAt = Instant.now();
}
