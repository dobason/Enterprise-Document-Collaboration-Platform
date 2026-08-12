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
import org.springframework.security.access.prepost.PreAuthorize;
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
@Tag(name = "ðŸ“ Folders", description = "Quáº£n lÃ½ thÆ° má»¥c lÆ°u trá»¯ tÃ i liá»‡u")
@SecurityRequirement(name = "bearerAuth")
public class FolderController {

    private final FolderApplicationService folderService;

    public FolderController(FolderApplicationService folderService) {
        this.folderService = folderService;
    }

    @GetMapping
    @Operation(summary = "Láº¥y danh sÃ¡ch táº¥t cáº£ thÆ° má»¥c", description = "Tráº£ vá» danh sÃ¡ch táº¥t cáº£ folder trong há»‡ thá»‘ng")
    public ResponseEntity<FolderListResponse> getAllFolders() {
        List<FolderDto> folders = folderService.getAllFolders();
        return ResponseEntity.ok(FolderListResponse.builder().items(folders).build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Láº¥y chi tiáº¿t thÆ° má»¥c", description = "Tráº£ vá» chi tiáº¿t cá»§a má»™t thÆ° má»¥c theo ID. MÃ£ máº«u: **f1**, **f2**")
    public ResponseEntity<FolderDto> getFolderById(
            @Parameter(description = "ID thÆ° má»¥c", example = "f1")
            @PathVariable("id") String id) {
        FolderDto folder = folderService.getFolderById(id);
        return ResponseEntity.ok(folder);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "Táº¡o thÆ° má»¥c má»›i",
        description = "Táº¡o má»™t thÆ° má»¥c má»›i trong há»‡ thá»‘ng",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "ðŸ“ ThÆ° Má»¥c Engineering", value = "{\"name\":\"Legal Contracts 2026\",\"department\":\"Engineering\"}"),
                    @ExampleObject(name = "ðŸ“ ThÆ° Má»¥c HR", value = "{\"name\":\"HR Policies 2026\",\"department\":\"HR\"}"),
                    @ExampleObject(name = "ðŸ“ ThÆ° Má»¥c Management", value = "{\"name\":\"Board Meeting Minutes\",\"department\":\"Management\"}")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "XÃ³a thÆ° má»¥c", description = "XÃ³a thÆ° má»¥c theo ID")
    public ResponseEntity<Void> deleteFolder(
            @Parameter(description = "ID thÆ° má»¥c cáº§n xÃ³a", example = "f2")
            @PathVariable("id") String id) {
        folderService.deleteFolder(id);
        return ResponseEntity.noContent().build();
    }
}
