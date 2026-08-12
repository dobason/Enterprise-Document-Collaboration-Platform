package com.edms.infrastructure.adapters.local;

import com.edms.api.exception.BadRequestException;
import com.edms.api.exception.ResourceNotFoundException;
import com.edms.application.ports.AuditService;
import com.edms.application.ports.WorkflowService;
import com.edms.domain.enums.ApprovalAction;
import com.edms.domain.enums.AuditAction;
import com.edms.domain.enums.DocumentStatus;
import com.edms.infrastructure.persistence.entity.ApprovalHistoryEntity;
import com.edms.infrastructure.persistence.entity.DocumentEntity;
import com.edms.infrastructure.persistence.repository.ApprovalHistoryJpaRepository;
import com.edms.infrastructure.persistence.repository.DocumentJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Profile({"mysql", "aws"})
public class LocalWorkflowService implements WorkflowService {

    private final DocumentJpaRepository documentRepository;
    private final ApprovalHistoryJpaRepository approvalHistoryRepository;
    private final AuditService auditService;

    public LocalWorkflowService(DocumentJpaRepository documentRepository,
                                ApprovalHistoryJpaRepository approvalHistoryRepository,
                                AuditService auditService) {
        this.documentRepository = documentRepository;
        this.approvalHistoryRepository = approvalHistoryRepository;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public void submitForApproval(String documentId, String submittedBy) {
        DocumentEntity doc = documentRepository.findByIdAndDeletedAtIsNull(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        if (doc.getStatus() != DocumentStatus.DRAFT && doc.getStatus() != DocumentStatus.PENDING) {
            throw new BadRequestException("Only DRAFT or PENDING documents can be submitted");
        }

        DocumentStatus oldStatus = doc.getStatus();
        doc.setStatus(DocumentStatus.PENDING);
        doc.setUpdatedAt(Instant.now());
        documentRepository.save(doc);

        saveHistory(documentId, ApprovalAction.SUBMIT, oldStatus, DocumentStatus.PENDING, submittedBy);
        auditService.log(documentId, AuditAction.SUBMIT, submittedBy, "Submitted document for approval");
    }

    @Override
    @Transactional
    public void approveDocument(String documentId, String approvedBy) {
        DocumentEntity doc = documentRepository.findByIdAndDeletedAtIsNull(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        // Accept both DRAFT (legacy) and PENDING
        if (doc.getStatus() != DocumentStatus.PENDING && doc.getStatus() != DocumentStatus.DRAFT) {
            throw new BadRequestException("Only PENDING documents can be approved");
        }

        DocumentStatus oldStatus = doc.getStatus();
        doc.setStatus(DocumentStatus.APPROVED);
        doc.setUpdatedAt(Instant.now());
        documentRepository.save(doc);

        saveHistory(documentId, ApprovalAction.APPROVE, oldStatus, DocumentStatus.APPROVED, approvedBy);
        auditService.log(documentId, AuditAction.APPROVE, approvedBy, "Approved document");
    }

    @Override
    @Transactional
    public void rejectDocument(String documentId, String rejectedBy, String reason) {
        DocumentEntity doc = documentRepository.findByIdAndDeletedAtIsNull(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        // Accept both DRAFT (legacy) and PENDING
        if (doc.getStatus() != DocumentStatus.PENDING && doc.getStatus() != DocumentStatus.DRAFT) {
            throw new BadRequestException("Only PENDING documents can be rejected");
        }

        DocumentStatus oldStatus = doc.getStatus();
        doc.setStatus(DocumentStatus.REJECTED);
        doc.setUpdatedAt(Instant.now());
        documentRepository.save(doc);

        saveHistory(documentId, ApprovalAction.REJECT, oldStatus, DocumentStatus.REJECTED, rejectedBy);
        auditService.log(documentId, AuditAction.REJECT, rejectedBy, "Rejected document: " + reason);
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
