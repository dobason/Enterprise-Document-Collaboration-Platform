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
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@Tag(name = "ðŸ“„ Documents", description = "Quáº£n lÃ½ tÃ i liá»‡u: táº¡o, xem, chá»‰nh sá»­a, xÃ³a")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentApplicationService documentService;

    public DocumentController(DocumentApplicationService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    @Operation(
        summary = "Láº¥y danh sÃ¡ch tÃ i liá»‡u (cÃ³ phÃ¢n trang)",
        description = "Tráº£ vá» danh sÃ¡ch táº¥t cáº£ tÃ i liá»‡u chÆ°a bá»‹ xÃ³a, há»— trá»£ phÃ¢n trang vÃ  sáº¯p xáº¿p"
    )
    public ResponseEntity<PageResponse<DocumentDto>> getDocuments(
            @Parameter(description = "Sá»‘ trang (báº¯t Ä‘áº§u tá»« 1)", example = "1")
            @RequestParam(name = "page", defaultValue = "1") int page,
            @Parameter(description = "Sá»‘ báº£n ghi má»—i trang", example = "10")
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @Parameter(description = "Sáº¯p xáº¿p theo trÆ°á»ng nÃ o (createdAt, title, status)", example = "createdAt")
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @Parameter(description = "Thá»© tá»± sáº¯p xáº¿p: asc hoáº·c desc", example = "desc")
            @RequestParam(name = "sortOrder", required = false) String sortOrder,
            @Parameter(description = "Lá»c theo thÆ° má»¥c", example = "f1")
            @RequestParam(name = "folderId", required = false) String folderId,
            Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        PageResponse<DocumentDto> response = documentService.getDocuments(page, limit, sortBy, sortOrder, folderId, currentUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Láº¥y chi tiáº¿t tÃ i liá»‡u",
        description = "Tráº£ vá» chi tiáº¿t má»™t tÃ i liá»‡u theo ID. MÃ£ tÃ i liá»‡u máº«u: **d1**, **d2**"
    )
    public ResponseEntity<DocumentDto> getDocumentById(
            @Parameter(description = "ID tÃ i liá»‡u", example = "d1")
            @PathVariable("id") String id, Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        DocumentDto doc = documentService.getDocumentById(id, currentUserId, isAdmin);
        return ResponseEntity.ok(doc);
    }

    @GetMapping("/{id}/download")
    @Operation(
        summary = "Táº£i file gá»‘c cá»§a tÃ i liá»‡u",
        description = "Tráº£ vá» ná»™i dung file Ä‘Ã£ upload (náº¿u cÃ³) kÃ¨m Content-Disposition attachment"
    )
    public ResponseEntity<byte[]> downloadDocument(
            @Parameter(description = "ID tÃ i liá»‡u", example = "d1")
            @PathVariable("id") String id, Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        DocumentApplicationService.FileDownload download = documentService.downloadDocument(id, currentUserId, isAdmin);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (download.contentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(download.contentType());
            } catch (IllegalArgumentException ignored) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(download.fileName(), java.nio.charset.StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(download.content());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Táº¡o tÃ i liá»‡u má»›i",
        description = "Táº¡o má»™t tÃ i liá»‡u má»›i á»Ÿ tráº¡ng thÃ¡i DRAFT. Tá»± Ä‘á»™ng táº¡o phiÃªn báº£n v1 vÃ  gÃ¡n quyá»n OWNER cho ngÆ°á»i táº¡o.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "ðŸ“Š BÃ¡o CÃ¡o TÃ i ChÃ­nh",
                        value = "{\"title\":\"BÃ¡o CÃ¡o TÃ i ChÃ­nh Q4 2026\",\"type\":\"Financial Report\",\"folderId\":\"f1\",\"content\":\"{\\\"total\\\":120000,\\\"currency\\\":\\\"VND\\\"}\"}"
                    ),
                    @ExampleObject(
                        name = "ðŸ“‹ Há»£p Äá»“ng Lao Äá»™ng",
                        value = "{\"title\":\"Há»£p Äá»“ng Lao Äá»™ng - Nguyen Van B\",\"type\":\"Contract\",\"folderId\":\"f2\",\"content\":\"{\\\"employee\\\":\\\"Nguyen Van B\\\",\\\"startDate\\\":\\\"2026-08-01\\\"}\"}"
                    ),
                    @ExampleObject(
                        name = "ðŸ“ Kiáº¿n TrÃºc Há»‡ Thá»‘ng",
                        value = "{\"title\":\"System Architecture Overview v2\",\"type\":\"Architecture Specification\",\"folderId\":\"f1\",\"content\":\"{\\\"version\\\":\\\"2.0\\\",\\\"architect\\\":\\\"Hexagonal Architecture\\\"}\"}"
                    )
                }
            )
        )
    )
    public ResponseEntity<DocumentDto> createDocument(@Valid @RequestBody CreateDocumentRequest request,
                                                      Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        DocumentDto doc = documentService.createDocument(request, currentUserId, isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(doc);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Cáº­p nháº­t tÃ i liá»‡u",
        description = "Cáº­p nháº­t tiÃªu Ä‘á», ná»™i dung hoáº·c folder cá»§a tÃ i liá»‡u. Chá»‰ cáº­p nháº­t cÃ¡c field Ä‘Æ°á»£c truyá»n vÃ o.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "ðŸ“ Cáº­p Nháº­t TiÃªu Äá»", value = "{\"title\":\"BÃ¡o CÃ¡o TÃ i ChÃ­nh Q4 2026 - Cáº­p Nháº­t\"}"),
                    @ExampleObject(name = "ðŸ“ Chuyá»ƒn Folder", value = "{\"folderId\":\"f2\"}"),
                    @ExampleObject(name = "ðŸ“„ Cáº­p Nháº­t Ná»™i Dung", value = "{\"content\":\"{\\\"total\\\":150000,\\\"status\\\":\\\"revised\\\"}\"}}")
                }
            )
        )
    )
    public ResponseEntity<DocumentDto> updateDocument(
            @Parameter(description = "ID tÃ i liá»‡u cáº§n cáº­p nháº­t", example = "d1")
            @PathVariable("id") String id,
            @RequestBody UpdateDocumentRequest request,
            Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        DocumentDto updated = documentService.updateDocument(id, request, currentUserId, isAdmin);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "XÃ³a tÃ i liá»‡u (Soft Delete)",
        description = "XÃ³a má»m tÃ i liá»‡u - gÃ¡n deletedAt timestamp, dá»¯ liá»‡u váº«n cÃ²n trong DB. MÃ£ tÃ i liá»‡u máº«u: **d2**"
    )
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "ID tÃ i liá»‡u cáº§n xÃ³a", example = "d2")
            @PathVariable("id") String id, Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        documentService.deleteDocument(id, currentUserId, isAdmin);
        return ResponseEntity.noContent().build();
    }
}
