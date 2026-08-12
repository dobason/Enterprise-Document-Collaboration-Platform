package com.edms.api.controller;

import com.edms.api.dto.CreateDocumentRequest;
import com.edms.api.dto.DocumentDto;
import com.edms.api.dto.PresignedUrlRequest;
import com.edms.api.dto.PresignedUrlResponse;
import com.edms.api.dto.UploadConfirmRequest;
import com.edms.application.ports.StorageService;
import com.edms.application.service.DocumentApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@Tag(name = "ðŸ“¤ Upload", description = "Upload tÃ i liá»‡u lÃªn há»‡ thá»‘ng qua Presigned URL")
@SecurityRequirement(name = "bearerAuth")
public class UploadController {

    private final StorageService storageService;
    private final DocumentApplicationService documentService;

    public UploadController(StorageService storageService, DocumentApplicationService documentService) {
        this.storageService = storageService;
        this.documentService = documentService;
    }

    @PostMapping("/url")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Táº¡o Presigned Upload URL",
        description = "Sinh ra má»™t Ä‘Æ°á»ng dáº«n upload táº¡m thá»i (Presigned URL) Ä‘á»ƒ client upload file trá»±c tiáº¿p lÃªn storage.\n\n" +
            "Trong mÃ´i trÆ°á»ng **local**, tráº£ vá» mock URL. Trong mÃ´i trÆ°á»ng **AWS**, tráº£ vá» S3 presigned URL thá»±c sá»±.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "ðŸ“„ Upload PDF", value = "{\"fileName\":\"contract-2026.pdf\",\"fileType\":\"application/pdf\"}"),
                    @ExampleObject(name = "ðŸ“Š Upload Excel", value = "{\"fileName\":\"budget-q4.xlsx\",\"fileType\":\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\"}"),
                    @ExampleObject(name = "ðŸ–¼ï¸ Upload PNG", value = "{\"fileName\":\"system-diagram.png\",\"fileType\":\"image/png\"}")
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

    @PutMapping("/mock-put/{fileId}")
    @Operation(
        summary = "Upload file (mock S3 PUT)",
        description = "Nháº­n raw bytes cá»§a file tá»« client vÃ  ghi vÃ o local storage (uploads/). " +
            "ÄÃ¢y lÃ  endpoint mÃ  presigned URL (trong mÃ´i trÆ°á»ng local) trá» tá»›i, thay cho S3 PUT tháº­t á»Ÿ Phase 2."
    )
    public ResponseEntity<Void> mockPutFile(
            @Parameter(description = "ID file táº¡m thá»i do /upload/url sinh ra", example = "uuid")
            @PathVariable("fileId") String fileId,
            @Parameter(description = "TÃªn file gá»‘c (chá»‰ láº¥y basename Ä‘á»ƒ trÃ¡nh path traversal)")
            @RequestParam(name = "fileName", required = false) String fileName,
            @RequestHeader(value = "Content-Type", required = false) String contentType,
            @RequestBody byte[] body) {
        String safeName = (fileName != null && !fileName.isBlank())
                ? Paths.get(fileName).getFileName().toString()
                : fileId;
        storageService.uploadFile(safeName, body,
                contentType != null ? contentType : "application/octet-stream");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "XÃ¡c nháº­n upload hoÃ n táº¥t & táº¡o Document",
        description = "Sau khi upload file thÃ nh cÃ´ng lÃªn storage, gá»i API nÃ y Ä‘á»ƒ táº¡o báº£n ghi Document trong há»‡ thá»‘ng.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "âœ… Confirm PDF Upload", value = "{\"fileId\":\"abc123-uuid\",\"fileName\":\"contract-2026.pdf\",\"fileType\":\"application/pdf\"}"),
                    @ExampleObject(name = "âœ… Confirm Excel Upload", value = "{\"fileId\":\"xyz789-uuid\",\"fileName\":\"budget-q4.xlsx\",\"fileType\":\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\"}")
                }
            )
        )
    )
    public ResponseEntity<DocumentDto> confirmUpload(@Valid @RequestBody UploadConfirmRequest request,
                                                      Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : request.getOwnerId();
        String safeName = (request.getFileName() != null && !request.getFileName().isBlank())
                ? Paths.get(request.getFileName()).getFileName().toString()
                : "uploaded-file";

        String contentStr = request.getExtractedContent();
        if (contentStr == null || contentStr.isBlank()) {
            contentStr = "{\"fileId\":\"" + request.getFileId() + "\",\"fileName\":\"" + safeName + "\"}";
        }

        String s3Key = storageService.buildKey(request.getFileId(), safeName);

        CreateDocumentRequest docReq = CreateDocumentRequest.builder()
                .title(safeName.replaceFirst("\\.[^.]+$", ""))
                .type(request.getFileType())
                .content(contentStr)
                .fileName(safeName)
                .fileType(request.getFileType())
                .s3Key(s3Key)
                .folderId(request.getFolderId())
                .build();

        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        DocumentDto doc = documentService.createDocument(docReq, currentUserId, isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(doc);
    }
}
