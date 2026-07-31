package com.edms.api.controller;

import com.edms.api.dto.AddTagRequest;
import com.edms.api.dto.DocTagDto;
import com.edms.api.dto.DocTagListResponse;
import com.edms.api.dto.TagListResponse;
import com.edms.application.service.TagApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "🏷️ Tags", description = "Quản lý nhãn (tag) và gán nhãn cho tài liệu")
@SecurityRequirement(name = "bearerAuth")
public class TagController {

    private final TagApplicationService tagService;

    public TagController(TagApplicationService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/documents/{id}/tags")
    @Operation(
        summary = "Lấy danh sách tags của tài liệu",
        description = "Trả về danh sách tất cả nhãn đã gán cho tài liệu. Mã tài liệu mẫu: **d1**"
    )
    public ResponseEntity<DocTagListResponse> getDocumentTags(
            @Parameter(description = "ID tài liệu", example = "d1")
            @PathVariable("id") String id) {
        DocTagListResponse response = tagService.getDocumentTags(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/documents/{id}/tags")
    @Operation(
        summary = "Gán nhãn cho tài liệu",
        description = "Thêm một nhãn mới vào tài liệu. Nếu nhãn chưa tồn tại sẽ tự động tạo mới.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "🔒 Nhãn Confidential", value = "{\"name\":\"Confidential\"}"),
                    @ExampleObject(name = "📋 Nhãn Legal", value = "{\"name\":\"Legal\"}"),
                    @ExampleObject(name = "💰 Nhãn Finance", value = "{\"name\":\"Finance\"}"),
                    @ExampleObject(name = "🔧 Nhãn Technical", value = "{\"name\":\"Technical\"}")
                }
            )
        )
    )
    public ResponseEntity<DocTagDto> addTagToDocument(
            @Parameter(description = "ID tài liệu", example = "d1")
            @PathVariable("id") String id,
            @Valid @RequestBody AddTagRequest request) {
        DocTagDto tag = tagService.addTagToDocument(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tag);
    }

    @DeleteMapping("/documents/{id}/tags/{docTagId}")
    @Operation(
        summary = "Xóa nhãn khỏi tài liệu",
        description = "Gỡ bỏ một nhãn khỏi tài liệu. Mã mẫu: id=**d1**, docTagId=**dt1**"
    )
    public ResponseEntity<Void> removeTagFromDocument(
            @Parameter(description = "ID tài liệu", example = "d1") @PathVariable("id") String id,
            @Parameter(description = "ID liên kết tag-document (docTagId)", example = "dt1") @PathVariable("docTagId") String docTagId) {
        tagService.removeTagFromDocument(id, docTagId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tags")
    @Operation(summary = "Lấy tất cả nhãn trong hệ thống", description = "Trả về danh sách tất cả nhãn hiện có để chọn gán cho tài liệu")
    public ResponseEntity<TagListResponse> getAllTags() {
        TagListResponse response = tagService.getAllTags();
        return ResponseEntity.ok(response);
    }
}
