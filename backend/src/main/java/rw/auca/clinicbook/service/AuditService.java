package rw.auca.clinicbook.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import rw.auca.clinicbook.document.AuditLog;
import rw.auca.clinicbook.repository.mongo.AuditLogRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(Long userId, String action, String entity, Object entityId) {
        try {
            String ip = null;
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
                ip = attrs.getRequest().getRemoteAddr();
            }
            auditLogRepository.save(AuditLog.builder()
                    .userId(userId).action(action).entity(entity)
                    .entityId(entityId == null ? null : entityId.toString())
                    .ip(ip).build());
        } catch (Exception e) {
            log.warn("Audit log failed: {}", e.getMessage()); // never break the main request
        }
    }
}
