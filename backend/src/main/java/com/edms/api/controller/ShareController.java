package com.edms.api.controller;

import com.edms.api.dto.CreateShareRequest;
import com.edms.api.dto.ShareDto;
import com.edms.api.dto.ShareLinkResponse;
import com.edms.api.dto.ShareListResponse;
import com.edms.application.service.ShareApplicationService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
@Tag(name = "ðŸ”— Sharing", description = "Chia sáº» tÃ i liá»‡u qua Ä‘Æ°á»ng dáº«n cÃ³ thá»i háº¡n")
@SecurityRequirement(name = "bearerAuth")
public class ShareController {

    private final ShareApplicationService shareService;

    public ShareController(ShareApplicationService shareService) {
        this.shareService = shareService;
    }

    @PostMapping("/{id}/share")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Táº¡o link chia sáº» tÃ i liá»‡u",
        description = "Táº¡o má»™t Ä‘Æ°á»ng dáº«n chia sáº» cÃ³ thá»i háº¡n cho tÃ i liá»‡u. ÄÆ°á»ng dáº«n tá»± háº¿t háº¡n sau sá»‘ giá» Ä‘Æ°á»£c chá»‰ Ä‘á»‹nh.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "ðŸ“§ Chia Sáº» 24 Giá»", value = "{\"email\":\"partner@client.com\",\"ttlHours\":24}"),
                    @ExampleObject(name = "ðŸ“§ Chia Sáº» 7 NgÃ y", value = "{\"email\":\"external.reviewer@company.vn\",\"ttlHours\":168}"),
                    @ExampleObject(name = "ðŸ“§ Chia Sáº» 1 Giá» (Táº¡m Thá»i)", value = "{\"email\":\"temp@meeting.com\",\"ttlHours\":1}")
                }
            )
        )
    )
    public ResponseEntity<ShareLinkResponse> createShareLink(
            @Parameter(description = "ID tÃ i liá»‡u cáº§n chia sáº»", example = "d1")
            @PathVariable("id") String id,
            @Valid @RequestBody CreateShareRequest request,
            Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        ShareLinkResponse response = shareService.createShareLink(id, request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/share")
    @Operation(
        summary = "Xem link chia sáº» hiá»‡n táº¡i",
        description = "Tráº£ vá» link chia sáº» Ä‘ang hoáº¡t Ä‘á»™ng má»›i nháº¥t cá»§a tÃ i liá»‡u. MÃ£ máº«u: **d1**"
    )
    public ResponseEntity<ShareDto> getShareByDocumentId(
            @Parameter(description = "ID tÃ i liá»‡u", example = "d1")
            @PathVariable("id") String id) {
        ShareDto share = shareService.getShareByDocumentId(id);
        return ResponseEntity.ok(share);
    }

    @GetMapping("/{id}/shares")
    @Operation(
        summary = "Xem toÃ n bá»™ lá»‹ch sá»­ chia sáº»",
        description = "Tráº£ vá» táº¥t cáº£ cÃ¡c link chia sáº» tá»«ng táº¡o cho tÃ i liá»‡u. MÃ£ máº«u: **d1**"
    )
    public ResponseEntity<ShareListResponse> getDocumentShares(
            @Parameter(description = "ID tÃ i liá»‡u", example = "d1")
            @PathVariable("id") String id) {
        ShareListResponse response = shareService.getDocumentShares(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/share/{token}")
    @Operation(
        summary = "Xem tÃ i liá»‡u qua link chia sáº»",
        description = "Public API Ä‘á»ƒ xem tÃ i liá»‡u báº±ng token chia sáº»"
    )
    public ResponseEntity<com.edms.api.dto.DocumentDto> getSharedDocument(
            @Parameter(description = "Share token")
            @PathVariable("token") String token) {
        com.edms.api.dto.DocumentDto doc = shareService.getSharedDocument(token);
        return ResponseEntity.ok(doc);
    }
}
