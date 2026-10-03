package rw.auca.clinicbook.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import rw.auca.clinicbook.document.NotificationLog;

public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {
}
