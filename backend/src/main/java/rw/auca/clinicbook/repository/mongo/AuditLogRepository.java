package rw.auca.clinicbook.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import rw.auca.clinicbook.document.AuditLog;

import java.util.List;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findTop50ByUserIdOrderByTimestampDesc(Long userId);
}
