package com.edms.api.controller;

import com.edms.api.dto.CreateFolderRequest;
import com.edms.api.dto.FolderDto;
import com.edms.api.dto.FolderListResponse;
import com.edms.application.service.FolderApplicationService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/folders")
@Tag(name = "📁 Folders", description = "Quản lý thư mục lưu trữ tài liệu")
@SecurityRequirement(name = "bearerAuth")
public class FolderController {

    private final FolderApplicationService folderService;

    public FolderController(FolderApplicationService folderService) {
        this.folderService = folderService;
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả thư mục", description = "Trả về danh sách tất cả folder trong hệ thống")
    public ResponseEntity<FolderListResponse> getAllFolders() {
        List<FolderDto> folders = folderService.getAllFolders();
        return ResponseEntity.ok(FolderListResponse.builder().items(folders).build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết thư mục", description = "Trả về chi tiết của một thư mục theo ID. Mã mẫu: **f1**, **f2**")
    public ResponseEntity<FolderDto> getFolderById(
            @Parameter(description = "ID thư mục", example = "f1")
            @PathVariable("id") String id) {
        FolderDto folder = folderService.getFolderById(id);
        return ResponseEntity.ok(folder);
    }

    @PostMapping
    @Operation(
        summary = "Tạo thư mục mới",
        description = "Tạo một thư mục mới trong hệ thống",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "📁 Thư Mục Engineering", value = "{\"name\":\"Legal Contracts 2026\",\"department\":\"Engineering\"}"),
                    @ExampleObject(name = "📁 Thư Mục HR", value = "{\"name\":\"HR Policies 2026\",\"department\":\"HR\"}"),
                    @ExampleObject(name = "📁 Thư Mục Management", value = "{\"name\":\"Board Meeting Minutes\",\"department\":\"Management\"}")
                }
            )
        )
    )
    public ResponseEntity<FolderDto> createFolder(@Valid @RequestBody CreateFolderRequest request,
                                                  Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        FolderDto created = folderService.createFolder(request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa thư mục", description = "Xóa thư mục theo ID")
    public ResponseEntity<Void> deleteFolder(
            @Parameter(description = "ID thư mục cần xóa", example = "f2")
            @PathVariable("id") String id) {
        folderService.deleteFolder(id);
        return ResponseEntity.noContent().build();
    }
}
