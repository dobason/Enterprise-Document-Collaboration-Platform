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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
@Tag(name = "🔗 Sharing", description = "Chia sẻ tài liệu qua đường dẫn có thời hạn")
@SecurityRequirement(name = "bearerAuth")
public class ShareController {

    private final ShareApplicationService shareService;

    public ShareController(ShareApplicationService shareService) {
        this.shareService = shareService;
    }

    @PostMapping("/{id}/share")
    @Operation(
        summary = "Tạo link chia sẻ tài liệu",
        description = "Tạo một đường dẫn chia sẻ có thời hạn cho tài liệu. Đường dẫn tự hết hạn sau số giờ được chỉ định.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "📧 Chia Sẻ 24 Giờ", value = "{\"email\":\"partner@client.com\",\"ttlHours\":24}"),
                    @ExampleObject(name = "📧 Chia Sẻ 7 Ngày", value = "{\"email\":\"external.reviewer@company.vn\",\"ttlHours\":168}"),
                    @ExampleObject(name = "📧 Chia Sẻ 1 Giờ (Tạm Thời)", value = "{\"email\":\"temp@meeting.com\",\"ttlHours\":1}")
                }
            )
        )
    )
    public ResponseEntity<ShareLinkResponse> createShareLink(
            @Parameter(description = "ID tài liệu cần chia sẻ", example = "d1")
            @PathVariable("id") String id,
            @Valid @RequestBody CreateShareRequest request,
            Authentication authentication) {
        String currentUserId = authentication != null ? authentication.getName() : "u1";
        ShareLinkResponse response = shareService.createShareLink(id, request, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/share")
    @Operation(
        summary = "Xem link chia sẻ hiện tại",
        description = "Trả về link chia sẻ đang hoạt động mới nhất của tài liệu. Mã mẫu: **d1**"
    )
    public ResponseEntity<ShareDto> getShareByDocumentId(
            @Parameter(description = "ID tài liệu", example = "d1")
            @PathVariable("id") String id) {
        ShareDto share = shareService.getShareByDocumentId(id);
        return ResponseEntity.ok(share);
    }

    @GetMapping("/{id}/shares")
    @Operation(
        summary = "Xem toàn bộ lịch sử chia sẻ",
        description = "Trả về tất cả các link chia sẻ từng tạo cho tài liệu. Mã mẫu: **d1**"
    )
    public ResponseEntity<ShareListResponse> getDocumentShares(
            @Parameter(description = "ID tài liệu", example = "d1")
            @PathVariable("id") String id) {
        ShareListResponse response = shareService.getDocumentShares(id);
        return ResponseEntity.ok(response);
    }
}
