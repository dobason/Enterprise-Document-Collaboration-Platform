package com.edms.api.controller;

import com.edms.api.dto.CreateDocumentRequest;
import com.edms.api.dto.DocumentDto;
import com.edms.api.dto.PageResponse;
import com.edms.api.dto.UpdateDocumentRequest;
import com.edms.application.service.DocumentApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
@Tag(name = "📄 Documents", description = "Quản lý tài liệu: tạo, xem, chỉnh sửa, xóa")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentApplicationService documentService;

    public DocumentController(DocumentApplicationService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    @Operation(
        summary = "Lấy danh sách tài liệu (có phân trang)",
        description = "Trả về danh sách tất cả tài liệu chưa bị xóa, hỗ trợ phân trang và sắp xếp"
    )
    public ResponseEntity<PageResponse<DocumentDto>> getDocuments(
            @Parameter(description = "Số trang (bắt đầu từ 1)", example = "1")
            @RequestParam(name = "page", defaultValue = "1") int page,
            @Parameter(description = "Số bản ghi mỗi trang", example = "10")
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @Parameter(description = "Sắp xếp theo trường nào (createdAt, title, status)", example = "createdAt")
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @Parameter(description = "Thứ tự sắp xếp: asc hoặc desc", example = "desc")
            @RequestParam(name = "sortOrder", required = false) String sortOrder,
            Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        PageResponse<DocumentDto> response = documentService.getDocuments(page, limit, sortBy, sortOrder, currentUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Lấy chi tiết tài liệu",
        description = "Trả về chi tiết một tài liệu theo ID. Mã tài liệu mẫu: **d1**, **d2**"
    )
    public ResponseEntity<DocumentDto> getDocumentById(
            @Parameter(description = "ID tài liệu", example = "d1")
            @PathVariable("id") String id, Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        DocumentDto doc = documentService.getDocumentById(id, currentUserId);
        return ResponseEntity.ok(doc);
    }

    @PostMapping
    @Operation(
        summary = "Tạo tài liệu mới",
        description = "Tạo một tài liệu mới ở trạng thái DRAFT. Tự động tạo phiên bản v1 và gán quyền OWNER cho người tạo.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "📊 Báo Cáo Tài Chính",
                        value = "{\"title\":\"Báo Cáo Tài Chính Q4 2026\",\"type\":\"Financial Report\",\"folderId\":\"f1\",\"content\":\"{\\\"total\\\":120000,\\\"currency\\\":\\\"VND\\\"}\"}"
                    ),
                    @ExampleObject(
                        name = "📋 Hợp Đồng Lao Động",
                        value = "{\"title\":\"Hợp Đồng Lao Động - Nguyen Van B\",\"type\":\"Contract\",\"folderId\":\"f2\",\"content\":\"{\\\"employee\\\":\\\"Nguyen Van B\\\",\\\"startDate\\\":\\\"2026-08-01\\\"}\"}"
                    ),
                    @ExampleObject(
                        name = "📐 Kiến Trúc Hệ Thống",
                        value = "{\"title\":\"System Architecture Overview v2\",\"type\":\"Architecture Specification\",\"folderId\":\"f1\",\"content\":\"{\\\"version\\\":\\\"2.0\\\",\\\"architect\\\":\\\"Hexagonal Architecture\\\"}\"}"
                    )
                }
            )
        )
    )
    public ResponseEntity<DocumentDto> createDocument(@Valid @RequestBody CreateDocumentRequest request,
                                                      Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        DocumentDto doc = documentService.createDocument(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(doc);
    }

    @PatchMapping("/{id}")
    @Operation(
        summary = "Cập nhật tài liệu",
        description = "Cập nhật tiêu đề, nội dung hoặc folder của tài liệu. Chỉ cập nhật các field được truyền vào.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "📝 Cập Nhật Tiêu Đề", value = "{\"title\":\"Báo Cáo Tài Chính Q4 2026 - Cập Nhật\"}"),
                    @ExampleObject(name = "📁 Chuyển Folder", value = "{\"folderId\":\"f2\"}"),
                    @ExampleObject(name = "📄 Cập Nhật Nội Dung", value = "{\"content\":\"{\\\"total\\\":150000,\\\"status\\\":\\\"revised\\\"}\"}}")
                }
            )
        )
    )
    public ResponseEntity<DocumentDto> updateDocument(
            @Parameter(description = "ID tài liệu cần cập nhật", example = "d1")
            @PathVariable("id") String id,
            @RequestBody UpdateDocumentRequest request,
            Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        DocumentDto updated = documentService.updateDocument(id, request, currentUserId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Xóa tài liệu (Soft Delete)",
        description = "Xóa mềm tài liệu - gán deletedAt timestamp, dữ liệu vẫn còn trong DB. Mã tài liệu mẫu: **d2**"
    )
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "ID tài liệu cần xóa", example = "d2")
            @PathVariable("id") String id, Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        documentService.deleteDocument(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
