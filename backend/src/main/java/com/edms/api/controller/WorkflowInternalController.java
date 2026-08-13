package com.edms.api.controller;

import com.edms.api.exception.ResourceNotFoundException;
import com.edms.domain.enums.ApprovalAction;
import com.edms.domain.enums.AuditAction;
import com.edms.domain.enums.DocumentStatus;
import com.edms.application.ports.AuditService;
import com.edms.infrastructure.persistence.entity.ApprovalHistoryEntity;
import com.edms.infrastructure.persistence.entity.DocumentEntity;
import com.edms.infrastructure.persistence.repository.ApprovalHistoryJpaRepository;
import com.edms.infrastructure.persistence.repository.DocumentJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/workflow")
@Profile("aws")
public class WorkflowInternalController {

    private static final Logger log = LoggerFactory.getLogger(WorkflowInternalController.class);

    private final DocumentJpaRepository documentRepository;
    private final ApprovalHistoryJpaRepository approvalHistoryRepository;
    private final AuditService auditService;

    public WorkflowInternalController(DocumentJpaRepository documentRepository,
                                      ApprovalHistoryJpaRepository approvalHistoryRepository,
                                      AuditService auditService) {
        this.documentRepository = documentRepository;
        this.approvalHistoryRepository = approvalHistoryRepository;
        this.auditService = auditService;
    }

    @PostMapping
    public Map<String, Object> handle(@RequestBody JsonNode payload) {
        String action = payload.path("edmsInternal").asText();

        if ("captureToken".equals(action)) {
            String documentId = payload.path("documentId").asText();
            String taskToken = payload.path("taskToken").asText();
            DocumentEntity doc = documentRepository.findById(documentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
            doc.setTaskToken(taskToken);
            documentRepository.save(doc);
            log.info("Captured Step Functions task token for document {}", documentId);
            return Map.of("ok", true, "documentId", documentId);
        }

        if ("markStatus".equals(action)) {
            String documentId = payload.path("documentId").asText();
            String decision = payload.path("decision").asText();
            String actedBy = payload.path("actedBy").asText("system");
            String reason = payload.path("reason").asText("");
            DocumentEntity doc = documentRepository.findByIdAndDeletedAtIsNull(documentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));
            DocumentStatus oldStatus = doc.getStatus();
            if ("APPROVED".equals(decision)) {
                doc.setStatus(DocumentStatus.APPROVED);
                doc.setUpdatedAt(Instant.now());
                documentRepository.save(doc);
                saveHistory(documentId, ApprovalAction.APPROVE, oldStatus, DocumentStatus.APPROVED, actedBy);
                auditService.log(documentId, AuditAction.APPROVE, actedBy, "Approved via Step Functions workflow");
                log.info("Step Functions marked document {} as APPROVED", documentId);
            } else if ("REJECTED".equals(decision)) {
                doc.setStatus(DocumentStatus.REJECTED);
                doc.setUpdatedAt(Instant.now());
                documentRepository.save(doc);
                saveHistory(documentId, ApprovalAction.REJECT, oldStatus, DocumentStatus.REJECTED, actedBy);
                auditService.log(documentId, AuditAction.REJECT, actedBy, "Rejected via Step Functions workflow: " + reason);
                log.info("Step Functions marked document {} as REJECTED", documentId);
            }
            return Map.of("ok", true, "documentId", documentId, "status", doc.getStatus().name());
        }

        return Map.of("ok", false, "error", "Unknown edmsInternal action: " + action);
    }

    private void saveHistory(String documentId, ApprovalAction action, DocumentStatus fromStatus, DocumentStatus toStatus, String user) {
        ApprovalHistoryEntity entity = ApprovalHistoryEntity.builder()
                .id(UUID.randomUUID().toString())
                .documentId(documentId)
                .action(action)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .performedBy(user != null ? user : "system")
                .timestamp(Instant.now())
                .build();
        approvalHistoryRepository.save(entity);
    }
}
