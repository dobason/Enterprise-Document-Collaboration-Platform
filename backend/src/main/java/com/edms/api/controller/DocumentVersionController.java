package com.edms.api.controller;

import com.edms.api.dto.CreateVersionRequest;
import com.edms.api.dto.RollbackVersionRequest;
import com.edms.api.dto.VersionDto;
import com.edms.api.dto.VersionListResponse;
import com.edms.application.service.DocumentVersionApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
@Tag(name = "📚 Document Versions", description = "Quản lý phiên bản tài liệu: tạo, xem, khôi phục")
@SecurityRequirement(name = "bearerAuth")
public class DocumentVersionController {

    private final DocumentVersionApplicationService versionService;

    public DocumentVersionController(DocumentVersionApplicationService versionService) {
        this.versionService = versionService;
    }

    @GetMapping("/{id}/versions")
    @Operation(
        summary = "Xem danh sách phiên bản",
        description = "Trả về tất cả phiên bản của tài liệu theo thứ tự mới nhất trước. Mã mẫu: **d1**"
    )
    public ResponseEntity<VersionListResponse> getVersions(
            @Parameter(description = "ID tài liệu", example = "d1")
            @PathVariable("id") String id) {
        VersionListResponse response = versionService.getVersions(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/versions")
    @Operation(
        summary = "Tạo phiên bản mới",
        description = "Tạo một phiên bản mới cho tài liệu (version số tự tăng). Tài liệu chính cũng được cập nhật nội dung theo.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "📄 Version Mới v2", value = "{\"content\":\"{\\\"architect\\\":\\\"Clean Architecture v2\\\",\\\"updated\\\":\\\"2026-07-31\\\"}\"}"),
                    @ExampleObject(name = "📄 Version Mới v3 với nội dung chi tiết", value = "{\"content\":\"{\\\"architect\\\":\\\"Clean Architecture v3\\\",\\\"layers\\\":[\\\"Domain\\\",\\\"Application\\\",\\\"Infrastructure\\\",\\\"API\\\"],\\\"status\\\":\\\"Draft\\\"}\"}")
                }
            )
        )
    )
    public ResponseEntity<VersionDto> createVersion(
            @Parameter(description = "ID tài liệu", example = "d1")
            @PathVariable("id") String id,
            @RequestBody CreateVersionRequest request,
            Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        VersionDto version = versionService.createVersion(id, request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(version);
    }

    @PostMapping("/{id}/versions/rollback")
    @Operation(
        summary = "Khôi phục phiên bản cũ",
        description = "Tạo một phiên bản mới với nội dung của phiên bản cũ (rollback không xóa lịch sử). Mã version mẫu: **v1**",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "⏪ Rollback về v1",
                    value = "{\"versionId\":\"v1\"}"
                )
            )
        )
    )
    public ResponseEntity<VersionDto> rollbackVersion(
            @Parameter(description = "ID tài liệu", example = "d1")
            @PathVariable("id") String id,
            @Valid @RequestBody RollbackVersionRequest request,
            Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        VersionDto version = versionService.rollbackVersion(id, request, currentUserId);
        return ResponseEntity.ok(version);
    }
}
