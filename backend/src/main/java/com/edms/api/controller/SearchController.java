package com.edms.api.controller;

import com.edms.api.dto.SearchResponse;
import com.edms.application.service.SearchApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@Tag(name = "🔍 Search", description = "Tìm kiếm tài liệu theo từ khóa, loại, trạng thái")
@SecurityRequirement(name = "bearerAuth")
public class SearchController {

    private final SearchApplicationService searchService;

    public SearchController(SearchApplicationService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    @Operation(
        summary = "Tìm kiếm tài liệu",
        description = "Tìm kiếm full-text theo tiêu đề và nội dung tài liệu, lọc theo loại hoặc trạng thái.\n\n" +
            "**Ví dụ:**\n" +
            "- Tìm theo từ khóa: `?q=architecture`\n" +
            "- Lọc theo trạng thái: `?status=APPROVED`\n" +
            "- Lọc theo loại: `?type=Financial+Report`\n" +
            "- Kết hợp: `?q=engineering&status=PENDING`"
    )
    public ResponseEntity<SearchResponse> search(
            @Parameter(description = "Từ khóa tìm kiếm trong tiêu đề và nội dung", example = "architecture")
            @RequestParam(name = "q", required = false) String query,
            @Parameter(description = "Lọc theo loại tài liệu", example = "Architecture Specification")
            @RequestParam(name = "type", required = false) String type,
            @Parameter(description = "Lọc theo trạng thái: DRAFT | PENDING | APPROVED | REJECTED", example = "APPROVED")
            @RequestParam(name = "status", required = false) String status,
            Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        SearchResponse response = searchService.searchDocuments(query, type, status, currentUserId, isAdmin);
        return ResponseEntity.ok(response);
    }
}
