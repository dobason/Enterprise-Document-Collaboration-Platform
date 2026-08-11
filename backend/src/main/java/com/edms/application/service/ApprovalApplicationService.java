package com.edms.application.service;

import com.edms.api.dto.ApprovalActionResponse;
import com.edms.api.dto.ApprovalHistoryDto;
import com.edms.api.dto.ApprovalHistoryListResponse;
import com.edms.api.dto.ApprovalSubmitResponse;
import com.edms.application.ports.WorkflowService;
import com.edms.infrastructure.persistence.entity.ApprovalHistoryEntity;
import com.edms.infrastructure.persistence.repository.ApprovalHistoryJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApprovalApplicationService {

    private final WorkflowService workflowService;
    private final ApprovalHistoryJpaRepository approvalHistoryRepository;

    public ApprovalApplicationService(WorkflowService workflowService,
                                      ApprovalHistoryJpaRepository approvalHistoryRepository) {
        this.workflowService = workflowService;
        this.approvalHistoryRepository = approvalHistoryRepository;
    }

    public ApprovalSubmitResponse submitForApproval(String documentId, String currentUserId) {
        workflowService.submitForApproval(documentId, currentUserId);
        return ApprovalSubmitResponse.builder()
                .id(documentId)
                .status("PENDING")
                .message("Submitted for approval")
                .build();
    }

    public ApprovalActionResponse approveDocument(String documentId, String currentUserId) {
        workflowService.approveDocument(documentId, currentUserId);
        return ApprovalActionResponse.builder()
                .id(documentId)
                .status("APPROVED")
                .build();
    }

    public ApprovalActionResponse rejectDocument(String documentId, String reason, String currentUserId) {
        workflowService.rejectDocument(documentId, currentUserId, reason);
        return ApprovalActionResponse.builder()
                .id(documentId)
                .status("REJECTED")
                .build();
    }

    @Transactional(readOnly = true)
    public ApprovalHistoryListResponse getApprovalHistory(String documentId) {
        List<ApprovalHistoryEntity> history = approvalHistoryRepository.findByDocumentIdOrderByTimestampAsc(documentId);
        List<ApprovalHistoryDto> dtos = history.stream()
                .map(h -> ApprovalHistoryDto.builder()
                        .id(h.getId())
                        .documentId(h.getDocumentId())
                        .action(h.getAction().name())
                        .fromStatus(h.getFromStatus().name())
                        .toStatus(h.getToStatus().name())
                        .performedBy(h.getPerformedBy())
                        .timestamp(h.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        return ApprovalHistoryListResponse.builder().items(dtos).build();
    }
}
