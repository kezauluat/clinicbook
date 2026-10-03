package rw.auca.clinicbook.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("audit_logs")
@CompoundIndex(name = "user_time", def = "{'userId': 1, 'timestamp': -1}")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    private String id;

    private Long userId;
    private String action;      // LOGIN, BOOK, CANCEL, ...
    private String entity;
    private String entityId;
    private String ip;

    @Indexed(expireAfter = "180d")
    @Builder.Default
    private Instant timestamp = Instant.now();
}
