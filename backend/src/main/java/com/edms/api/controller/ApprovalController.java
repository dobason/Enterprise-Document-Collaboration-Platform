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
@Tag(name = "✅ Approval Workflow", description = "Quy trình phê duyệt tài liệu: Submit, Approve, Reject")
@SecurityRequirement(name = "bearerAuth")
public class ApprovalController {

    private final ApprovalApplicationService approvalService;

    public ApprovalController(ApprovalApplicationService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping("/submit")
    @Operation(
        summary = "Nộp tài liệu để phê duyệt",
        description = "Chuyển tài liệu từ trạng thái **DRAFT** sang **PENDING**. Yêu cầu đăng nhập với quyền OWNER hoặc EDITOR.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "📤 Submit Document",
                    summary = "Nộp tài liệu d2 để phê duyệt",
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
    @Operation(
        summary = "Phê duyệt tài liệu",
        description = "Chuyển tài liệu từ trạng thái **PENDING** sang **APPROVED**. Yêu cầu quyền MANAGER hoặc ADMIN.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "✅ Approve Document",
                    summary = "Phê duyệt tài liệu d2",
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
    @Operation(
        summary = "Từ chối tài liệu",
        description = "Chuyển tài liệu từ trạng thái **PENDING** sang **REJECTED** với lý do. Yêu cầu quyền MANAGER hoặc ADMIN.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "❌ Reject Document",
                    summary = "Từ chối tài liệu d2 với lý do",
                    value = "{\"documentId\":\"d2\",\"reason\":\"Nội dung thiếu chữ ký phê duyệt cấp trên\"}"
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
        summary = "Xem lịch sử phê duyệt của tài liệu",
        description = "Trả về toàn bộ lịch sử hành động phê duyệt (Submit, Approve, Reject) theo thứ tự thời gian tăng dần"
    )
    public ResponseEntity<ApprovalHistoryListResponse> getApprovalHistory(
            @Parameter(description = "ID tài liệu cần xem lịch sử", example = "d1")
            @RequestParam("documentId") String documentId) {
        ApprovalHistoryListResponse history = approvalService.getApprovalHistory(documentId);
        return ResponseEntity.ok(history);
    }
}
