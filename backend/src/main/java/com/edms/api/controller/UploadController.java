package com.edms.api.controller;

import com.edms.api.dto.CreateDocumentRequest;
import com.edms.api.dto.DocumentDto;
import com.edms.api.dto.PresignedUrlRequest;
import com.edms.api.dto.PresignedUrlResponse;
import com.edms.api.dto.UploadConfirmRequest;
import com.edms.application.ports.StorageService;
import com.edms.application.service.DocumentApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@Tag(name = "📤 Upload", description = "Upload tài liệu lên hệ thống qua Presigned URL")
@SecurityRequirement(name = "bearerAuth")
public class UploadController {

    private final StorageService storageService;
    private final DocumentApplicationService documentService;

    public UploadController(StorageService storageService, DocumentApplicationService documentService) {
        this.storageService = storageService;
        this.documentService = documentService;
    }

    @PostMapping("/url")
    @Operation(
        summary = "Tạo Presigned Upload URL",
        description = "Sinh ra một đường dẫn upload tạm thời (Presigned URL) để client upload file trực tiếp lên storage.\n\n" +
            "Trong môi trường **local**, trả về mock URL. Trong môi trường **AWS**, trả về S3 presigned URL thực sự.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "📄 Upload PDF", value = "{\"fileName\":\"contract-2026.pdf\",\"fileType\":\"application/pdf\"}"),
                    @ExampleObject(name = "📊 Upload Excel", value = "{\"fileName\":\"budget-q4.xlsx\",\"fileType\":\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\"}"),
                    @ExampleObject(name = "🖼️ Upload PNG", value = "{\"fileName\":\"system-diagram.png\",\"fileType\":\"image/png\"}")
                }
            )
        )
    )
    public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(@Valid @RequestBody PresignedUrlRequest request) {
        String fileId = UUID.randomUUID().toString();
        String url = storageService.generatePresignedUploadUrl(fileId, request.getFileName(), request.getFileType());

        PresignedUrlResponse response = PresignedUrlResponse.builder()
                .url(url)
                .fileId(fileId)
                .fields(Map.of("key", "uploads/" + request.getFileName(), "Content-Type", request.getFileType()))
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm")
    @Operation(
        summary = "Xác nhận upload hoàn tất & tạo Document",
        description = "Sau khi upload file thành công lên storage, gọi API này để tạo bản ghi Document trong hệ thống.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "✅ Confirm PDF Upload", value = "{\"fileId\":\"abc123-uuid\",\"fileName\":\"contract-2026.pdf\",\"fileType\":\"application/pdf\"}"),
                    @ExampleObject(name = "✅ Confirm Excel Upload", value = "{\"fileId\":\"xyz789-uuid\",\"fileName\":\"budget-q4.xlsx\",\"fileType\":\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\"}")
                }
            )
        )
    )
    public ResponseEntity<DocumentDto> confirmUpload(@Valid @RequestBody UploadConfirmRequest request,
                                                      Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : request.getOwnerId();

        CreateDocumentRequest docReq = CreateDocumentRequest.builder()
                .title(request.getFileName())
                .type(request.getFileType())
                .content("{\"fileId\":\"" + request.getFileId() + "\",\"fileName\":\"" + request.getFileName() + "\"}")
                .build();

        DocumentDto doc = documentService.createDocument(docReq, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(doc);
    }
}
