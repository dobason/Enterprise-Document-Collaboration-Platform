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
@Tag(name = "🔑 Permissions", description = "Quản lý quyền truy cập tài liệu: Grant, Update, Revoke")
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {

    private final PermissionApplicationService permissionService;

    public PermissionController(PermissionApplicationService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/{id}/permissions")
    @Operation(
        summary = "Xem danh sách quyền của tài liệu",
        description = "Trả về danh sách người dùng và quyền tương ứng cho tài liệu. Mã tài liệu mẫu: **d1**"
    )
    public ResponseEntity<PermissionListResponse> getPermissions(
            @Parameter(description = "ID tài liệu", example = "d1")
            @PathVariable("id") String id) {
        PermissionListResponse response = permissionService.getPermissions(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/permissions")
    @Operation(
        summary = "Gán quyền cho người dùng",
        description = "Gán hoặc cập nhật quyền truy cập cho một người dùng trên tài liệu.\n\n" +
            "**Các cấp quyền**: `OWNER` | `EDITOR` | `VIEWER`",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "👁️ Cấp quyền VIEWER cho Viewer (u4)", value = "{\"userId\":\"u4\",\"role\":\"VIEWER\"}"),
                    @ExampleObject(name = "✏️ Cấp quyền EDITOR cho Editor (u2)", value = "{\"userId\":\"u2\",\"role\":\"EDITOR\"}"),
                    @ExampleObject(name = "📋 Cấp quyền VIEWER cho Manager (u3)", value = "{\"userId\":\"u3\",\"role\":\"VIEWER\"}")
                }
            )
        )
    )
    public ResponseEntity<PermissionDto> grantPermission(
            @Parameter(description = "ID tài liệu", example = "d1")
            @PathVariable("id") String id,
            @Valid @RequestBody GrantPermissionRequest request) {
        PermissionDto perm = permissionService.grantPermission(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(perm);
    }

    @PutMapping("/{id}/permissions/{permissionId}")
    @Operation(
        summary = "Cập nhật quyền",
        description = "Thay đổi cấp quyền của một user trên tài liệu. Mã permission mẫu: **p2**, **p3**",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "Nâng lên EDITOR", value = "{\"role\":\"EDITOR\"}"),
                    @ExampleObject(name = "Hạ xuống VIEWER", value = "{\"role\":\"VIEWER\"}")
                }
            )
        )
    )
    public ResponseEntity<PermissionDto> updatePermission(
            @Parameter(description = "ID tài liệu", example = "d1") @PathVariable("id") String id,
            @Parameter(description = "ID quyền cần cập nhật", example = "p2") @PathVariable("permissionId") String permissionId,
            @Valid @RequestBody UpdatePermissionRequest request) {
        PermissionDto updated = permissionService.updatePermission(id, permissionId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/permissions/{permissionId}")
    @Operation(
        summary = "Thu hồi quyền",
        description = "Xóa quyền truy cập của một user trên tài liệu. Mã permission mẫu: **p3**"
    )
    public ResponseEntity<Void> revokePermission(
            @Parameter(description = "ID tài liệu", example = "d1") @PathVariable("id") String id,
            @Parameter(description = "ID quyền cần thu hồi", example = "p3") @PathVariable("permissionId") String permissionId) {
        permissionService.revokePermission(id, permissionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/permissions/role")
    @Operation(
        summary = "Xem quyền của bản thân trên tài liệu",
        description = "Trả về role của người dùng đang đăng nhập trên tài liệu cụ thể"
    )
    public ResponseEntity<UserRoleResponse> getUserRoleForDocument(
            @Parameter(description = "ID tài liệu", example = "d1")
            @PathVariable("id") String id,
            Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        UserRoleResponse response = permissionService.getUserRoleForDocument(id, currentUserId);
        return ResponseEntity.ok(response);
    }
}
