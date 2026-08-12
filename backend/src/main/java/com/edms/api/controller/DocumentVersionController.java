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
@Tag(name = "ðŸ“š Document Versions", description = "Quáº£n lÃ½ phiÃªn báº£n tÃ i liá»‡u: táº¡o, xem, khÃ´i phá»¥c")
@SecurityRequirement(name = "bearerAuth")
public class DocumentVersionController {

    private final DocumentVersionApplicationService versionService;

    public DocumentVersionController(DocumentVersionApplicationService versionService) {
        this.versionService = versionService;
    }

    @GetMapping("/{id}/versions")
    @Operation(
        summary = "Xem danh sÃ¡ch phiÃªn báº£n",
        description = "Tráº£ vá» táº¥t cáº£ phiÃªn báº£n cá»§a tÃ i liá»‡u theo thá»© tá»± má»›i nháº¥t trÆ°á»›c. MÃ£ máº«u: **d1**"
    )
    public ResponseEntity<VersionListResponse> getVersions(
            @Parameter(description = "ID tÃ i liá»‡u", example = "d1")
            @PathVariable("id") String id) {
        VersionListResponse response = versionService.getVersions(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/versions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Táº¡o phiÃªn báº£n má»›i",
        description = "Táº¡o má»™t phiÃªn báº£n má»›i cho tÃ i liá»‡u (version sá»‘ tá»± tÄƒng). TÃ i liá»‡u chÃ­nh cÅ©ng Ä‘Æ°á»£c cáº­p nháº­t ná»™i dung theo.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "ðŸ“„ Version Má»›i v2", value = "{\"content\":\"{\\\"architect\\\":\\\"Clean Architecture v2\\\",\\\"updated\\\":\\\"2026-07-31\\\"}\"}"),
                    @ExampleObject(name = "ðŸ“„ Version Má»›i v3 vá»›i ná»™i dung chi tiáº¿t", value = "{\"content\":\"{\\\"architect\\\":\\\"Clean Architecture v3\\\",\\\"layers\\\":[\\\"Domain\\\",\\\"Application\\\",\\\"Infrastructure\\\",\\\"API\\\"],\\\"status\\\":\\\"Draft\\\"}\"}")
                }
            )
        )
    )
    public ResponseEntity<VersionDto> createVersion(
            @Parameter(description = "ID tÃ i liá»‡u", example = "d1")
            @PathVariable("id") String id,
            @RequestBody CreateVersionRequest request,
            Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        VersionDto version = versionService.createVersion(id, request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(version);
    }

    @PostMapping("/{id}/versions/rollback")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "KhÃ´i phá»¥c phiÃªn báº£n cÅ©",
        description = "Táº¡o má»™t phiÃªn báº£n má»›i vá»›i ná»™i dung cá»§a phiÃªn báº£n cÅ© (rollback khÃ´ng xÃ³a lá»‹ch sá»­). MÃ£ version máº«u: **v1**",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "âª Rollback vá» v1",
                    value = "{\"versionId\":\"v1\"}"
                )
            )
        )
    )
    public ResponseEntity<VersionDto> rollbackVersion(
            @Parameter(description = "ID tÃ i liá»‡u", example = "d1")
            @PathVariable("id") String id,
            @Valid @RequestBody RollbackVersionRequest request,
            Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        VersionDto version = versionService.rollbackVersion(id, request, currentUserId);
        return ResponseEntity.ok(version);
    }
}
