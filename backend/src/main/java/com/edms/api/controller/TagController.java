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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "ðŸ·ï¸ Tags", description = "Quáº£n lÃ½ nhÃ£n (tag) vÃ  gÃ¡n nhÃ£n cho tÃ i liá»‡u")
@SecurityRequirement(name = "bearerAuth")
public class TagController {

    private final TagApplicationService tagService;

    public TagController(TagApplicationService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/documents/{id}/tags")
    @Operation(
        summary = "Láº¥y danh sÃ¡ch tags cá»§a tÃ i liá»‡u",
        description = "Tráº£ vá» danh sÃ¡ch táº¥t cáº£ nhÃ£n Ä‘Ã£ gÃ¡n cho tÃ i liá»‡u. MÃ£ tÃ i liá»‡u máº«u: **d1**"
    )
    public ResponseEntity<DocTagListResponse> getDocumentTags(
            @Parameter(description = "ID tÃ i liá»‡u", example = "d1")
            @PathVariable("id") String id) {
        DocTagListResponse response = tagService.getDocumentTags(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/documents/{id}/tags")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "GÃ¡n nhÃ£n cho tÃ i liá»‡u",
        description = "ThÃªm má»™t nhÃ£n má»›i vÃ o tÃ i liá»‡u. Náº¿u nhÃ£n chÆ°a tá»“n táº¡i sáº½ tá»± Ä‘á»™ng táº¡o má»›i.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "ðŸ”’ NhÃ£n Confidential", value = "{\"name\":\"Confidential\"}"),
                    @ExampleObject(name = "ðŸ“‹ NhÃ£n Legal", value = "{\"name\":\"Legal\"}"),
                    @ExampleObject(name = "ðŸ’° NhÃ£n Finance", value = "{\"name\":\"Finance\"}"),
                    @ExampleObject(name = "ðŸ”§ NhÃ£n Technical", value = "{\"name\":\"Technical\"}")
                }
            )
        )
    )
    public ResponseEntity<DocTagDto> addTagToDocument(
            @Parameter(description = "ID tÃ i liá»‡u", example = "d1")
            @PathVariable("id") String id,
            @Valid @RequestBody AddTagRequest request) {
        DocTagDto tag = tagService.addTagToDocument(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tag);
    }

    @DeleteMapping("/documents/{id}/tags/{docTagId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(
        summary = "XÃ³a nhÃ£n khá»i tÃ i liá»‡u",
        description = "Gá»¡ bá» má»™t nhÃ£n khá»i tÃ i liá»‡u. MÃ£ máº«u: id=**d1**, docTagId=**dt1**"
    )
    public ResponseEntity<Void> removeTagFromDocument(
            @Parameter(description = "ID tÃ i liá»‡u", example = "d1") @PathVariable("id") String id,
            @Parameter(description = "ID liÃªn káº¿t tag-document (docTagId)", example = "dt1") @PathVariable("docTagId") String docTagId) {
        tagService.removeTagFromDocument(id, docTagId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tags")
    @Operation(summary = "Láº¥y táº¥t cáº£ nhÃ£n trong há»‡ thá»‘ng", description = "Tráº£ vá» danh sÃ¡ch táº¥t cáº£ nhÃ£n hiá»‡n cÃ³ Ä‘á»ƒ chá»n gÃ¡n cho tÃ i liá»‡u")
    public ResponseEntity<TagListResponse> getAllTags() {
        TagListResponse response = tagService.getAllTags();
        return ResponseEntity.ok(response);
    }
}
