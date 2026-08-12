package com.edms.api.controller;

import com.edms.api.dto.ApprovalActionResponse;
import com.edms.api.dto.ApprovalHistoryListResponse;
import com.edms.api.dto.ApprovalSubmitResponse;
import com.edms.application.service.ApprovalApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/approval")
@Tag(name = "âœ… Approval Workflow", description = "Quy trÃ¬nh phÃª duyá»‡t tÃ i liá»‡u: Submit, Approve, Reject")
@SecurityRequirement(name = "bearerAuth")
public class ApprovalController {

    private final ApprovalApplicationService approvalService;

    public ApprovalController(ApprovalApplicationService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Ná»™p tÃ i liá»‡u Ä‘á»ƒ phÃª duyá»‡t",
        description = "Chuyá»ƒn tÃ i liá»‡u tá»« tráº¡ng thÃ¡i **DRAFT** sang **PENDING**. YÃªu cáº§u Ä‘Äƒng nháº­p vá»›i quyá»n OWNER hoáº·c EDITOR.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "ðŸ“¤ Submit Document",
                    summary = "Ná»™p tÃ i liá»‡u d2 Ä‘á»ƒ phÃª duyá»‡t",
                    value = "{\"documentId\":\"d2\"}"
                )
            )
        )
    )
    public ResponseEntity<ApprovalSubmitResponse> submitForApproval(@RequestBody Map<String, String> body,
                                                                    Authentication authentication) {
        String documentId = body.get("documentId");
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        ApprovalSubmitResponse response = approvalService.submitForApproval(documentId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "PhÃª duyá»‡t tÃ i liá»‡u",
        description = "Chuyá»ƒn tÃ i liá»‡u tá»« tráº¡ng thÃ¡i **PENDING** sang **APPROVED**. YÃªu cáº§u quyá»n MANAGER hoáº·c ADMIN.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "âœ… Approve Document",
                    summary = "PhÃª duyá»‡t tÃ i liá»‡u d2",
                    value = "{\"documentId\":\"d2\"}"
                )
            )
        )
    )
    public ResponseEntity<ApprovalActionResponse> approveDocument(@RequestBody Map<String, String> body,
                                                                  Authentication authentication) {
        String documentId = body.get("documentId");
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        ApprovalActionResponse response = approvalService.approveDocument(documentId, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(
        summary = "Tá»« chá»‘i tÃ i liá»‡u",
        description = "Chuyá»ƒn tÃ i liá»‡u tá»« tráº¡ng thÃ¡i **PENDING** sang **REJECTED** vá»›i lÃ½ do. YÃªu cáº§u quyá»n MANAGER hoáº·c ADMIN.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "âŒ Reject Document",
                    summary = "Tá»« chá»‘i tÃ i liá»‡u d2 vá»›i lÃ½ do",
                    value = "{\"documentId\":\"d2\",\"reason\":\"Ná»™i dung thiáº¿u chá»¯ kÃ½ phÃª duyá»‡t cáº¥p trÃªn\"}"
                )
            )
        )
    )
    public ResponseEntity<ApprovalActionResponse> rejectDocument(@RequestBody Map<String, String> body,
                                                                 Authentication authentication) {
        String documentId = body.get("documentId");
        String reason = body.getOrDefault("reason", "No reason specified");
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        ApprovalActionResponse response = approvalService.rejectDocument(documentId, reason, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @Operation(
        summary = "Xem lá»‹ch sá»­ phÃª duyá»‡t cá»§a tÃ i liá»‡u",
        description = "Tráº£ vá» toÃ n bá»™ lá»‹ch sá»­ hÃ nh Ä‘á»™ng phÃª duyá»‡t (Submit, Approve, Reject) theo thá»© tá»± thá»i gian tÄƒng dáº§n"
    )
    public ResponseEntity<ApprovalHistoryListResponse> getApprovalHistory(
            @Parameter(description = "ID tÃ i liá»‡u cáº§n xem lá»‹ch sá»­", example = "d1")
            @RequestParam("documentId") String documentId) {
        ApprovalHistoryListResponse history = approvalService.getApprovalHistory(documentId);
        return ResponseEntity.ok(history);
    }
}
