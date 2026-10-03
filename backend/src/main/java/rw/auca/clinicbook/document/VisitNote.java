package rw.auca.clinicbook.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document("visit_notes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VisitNote {

    @Id
    private String id;

    @Indexed(unique = true)
    private Long appointmentId;

    @Indexed
    private Long patientId;

    private Long doctorId;

    private String notes;

    @Builder.Default
    private List<String> prescriptions = new ArrayList<>();

    @Builder.Default
    private List<String> attachments = new ArrayList<>();

    @Builder.Default
    private Instant createdAt = Instant.now();
}
