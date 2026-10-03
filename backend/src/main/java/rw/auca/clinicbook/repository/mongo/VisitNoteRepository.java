package rw.auca.clinicbook.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import rw.auca.clinicbook.document.VisitNote;

import java.util.List;
import java.util.Optional;

public interface VisitNoteRepository extends MongoRepository<VisitNote, String> {
    Optional<VisitNote> findByAppointmentId(Long appointmentId);
    List<VisitNote> findByPatientIdOrderByCreatedAtDesc(Long patientId);
}
