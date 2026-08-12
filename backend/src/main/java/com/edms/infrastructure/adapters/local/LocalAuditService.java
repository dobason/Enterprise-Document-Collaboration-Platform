package com.edms.infrastructure.adapters.local;

import com.edms.application.ports.AuditService;
import com.edms.domain.enums.AuditAction;
import com.edms.infrastructure.persistence.entity.AuditLogEntity;
import com.edms.infrastructure.persistence.repository.AuditLogJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@Profile({"mysql", "aws"})
public class LocalAuditService implements AuditService {

    private static final Logger log = LoggerFactory.getLogger(LocalAuditService.class);
    private final AuditLogJpaRepository auditLogRepository;

    public LocalAuditService(AuditLogJpaRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void log(String documentId, AuditAction action, String performedBy, String details) {
        log.info("AUDIT LOG: docId={}, action={}, user={}, details={}", documentId, action, performedBy, details);
        AuditLogEntity entity = AuditLogEntity.builder()
                .id(UUID.randomUUID().toString())
                .documentId(documentId)
                .action(action)
                .performedBy(performedBy != null ? performedBy : "system")
                .details(details)
                .timestamp(Instant.now())
                .build();
        auditLogRepository.save(entity);
    }
}
