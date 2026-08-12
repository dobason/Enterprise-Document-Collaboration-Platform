package com.edms.api.controller;

import com.edms.api.dto.GrantPermissionRequest;
import com.edms.api.dto.PermissionDto;
import com.edms.api.dto.PermissionListResponse;
import com.edms.api.dto.UpdatePermissionRequest;
import com.edms.api.dto.UserRoleResponse;
import com.edms.application.service.PermissionApplicationService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
@Tag(name = "ðŸ”‘ Permissions", description = "Quáº£n lÃ½ quyá»n truy cáº­p tÃ i liá»‡u: Grant, Update, Revoke")
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {

    private final PermissionApplicationService permissionService;

    public PermissionController(PermissionApplicationService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/{id}/permissions")
    @Operation(
        summary = "Xem danh sÃ¡ch quyá»n cá»§a tÃ i liá»‡u",
        description = "Tráº£ vá» danh sÃ¡ch ngÆ°á»i dÃ¹ng vÃ  quyá»n tÆ°Æ¡ng á»©ng cho tÃ i liá»‡u. MÃ£ tÃ i liá»‡u máº«u: **d1**"
    )
    public ResponseEntity<PermissionListResponse> getPermissions(
            @Parameter(description = "ID tÃ i liá»‡u", example = "d1")
            @PathVariable("id") String id) {
        PermissionListResponse response = permissionService.getPermissions(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "GÃ¡n quyá»n cho ngÆ°á»i dÃ¹ng",
        description = "GÃ¡n hoáº·c cáº­p nháº­t quyá»n truy cáº­p cho má»™t ngÆ°á»i dÃ¹ng trÃªn tÃ i liá»‡u.\n\n" +
            "**CÃ¡c cáº¥p quyá»n**: `OWNER` | `EDITOR` | `VIEWER`",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "ðŸ‘ï¸ Cáº¥p quyá»n VIEWER cho Viewer (u4)", value = "{\"userId\":\"u4\",\"role\":\"VIEWER\"}"),
                    @ExampleObject(name = "âœï¸ Cáº¥p quyá»n EDITOR cho Editor (u2)", value = "{\"userId\":\"u2\",\"role\":\"EDITOR\"}"),
                    @ExampleObject(name = "ðŸ“‹ Cáº¥p quyá»n VIEWER cho Manager (u3)", value = "{\"userId\":\"u3\",\"role\":\"VIEWER\"}")
                }
            )
        )
    )
    public ResponseEntity<PermissionDto> grantPermission(
            @Parameter(description = "ID tÃ i liá»‡u", example = "d1")
            @PathVariable("id") String id,
            @Valid @RequestBody GrantPermissionRequest request) {
        PermissionDto perm = permissionService.grantPermission(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(perm);
    }

    @PutMapping("/{id}/permissions/{permissionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Cáº­p nháº­t quyá»n",
        description = "Thay Ä‘á»•i cáº¥p quyá»n cá»§a má»™t user trÃªn tÃ i liá»‡u. MÃ£ permission máº«u: **p2**, **p3**",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "NÃ¢ng lÃªn EDITOR", value = "{\"role\":\"EDITOR\"}"),
                    @ExampleObject(name = "Háº¡ xuá»‘ng VIEWER", value = "{\"role\":\"VIEWER\"}")
                }
            )
        )
    )
    public ResponseEntity<PermissionDto> updatePermission(
            @Parameter(description = "ID tÃ i liá»‡u", example = "d1") @PathVariable("id") String id,
            @Parameter(description = "ID quyá»n cáº§n cáº­p nháº­t", example = "p2") @PathVariable("permissionId") String permissionId,
            @Valid @RequestBody UpdatePermissionRequest request) {
        PermissionDto updated = permissionService.updatePermission(id, permissionId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/permissions/{permissionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Thu há»“i quyá»n",
        description = "XÃ³a quyá»n truy cáº­p cá»§a má»™t user trÃªn tÃ i liá»‡u. MÃ£ permission máº«u: **p3**"
    )
    public ResponseEntity<Void> revokePermission(
            @Parameter(description = "ID tÃ i liá»‡u", example = "d1") @PathVariable("id") String id,
            @Parameter(description = "ID quyá»n cáº§n thu há»“i", example = "p3") @PathVariable("permissionId") String permissionId) {
        permissionService.revokePermission(id, permissionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/permissions/role")
    @Operation(
        summary = "Xem quyá»n cá»§a báº£n thÃ¢n trÃªn tÃ i liá»‡u",
        description = "Tráº£ vá» role cá»§a ngÆ°á»i dÃ¹ng Ä‘ang Ä‘Äƒng nháº­p trÃªn tÃ i liá»‡u cá»¥ thá»ƒ"
    )
    public ResponseEntity<UserRoleResponse> getUserRoleForDocument(
            @Parameter(description = "ID tÃ i liá»‡u", example = "d1")
            @PathVariable("id") String id,
            Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        UserRoleResponse response = permissionService.getUserRoleForDocument(id, currentUserId);
        return ResponseEntity.ok(response);
    }
}
