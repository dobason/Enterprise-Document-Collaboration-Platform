package com.edms.api.controller;

import com.edms.api.dto.OcrResultResponse;
import com.edms.application.service.OcrApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
@Tag(name = "🤖 OCR", description = "Nhận dạng văn bản tự động (OCR) từ nội dung tài liệu")
@SecurityRequirement(name = "bearerAuth")
public class OcrController {

    private final OcrApplicationService ocrService;

    public OcrController(OcrApplicationService ocrService) {
        this.ocrService = ocrService;
    }

    @GetMapping("/{id}/ocr")
    @Operation(
        summary = "Lấy kết quả OCR hiện có",
        description = "Trả về kết quả trích xuất văn bản đã được xử lý trước đó của tài liệu. Mã mẫu: **d1** (đã có kết quả)"
    )
    public ResponseEntity<OcrResultResponse> getOcrResult(
            @Parameter(description = "ID tài liệu", example = "d1")
            @PathVariable("id") String id) {
        OcrResultResponse response = ocrService.getOcrResult(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/ocr")
    @Operation(
        summary = "Kích hoạt xử lý OCR",
        description = "Gửi yêu cầu trích xuất văn bản (OCR) từ file tài liệu. Trong môi trường local, trả về mock text.\n\n" +
            "Mã tài liệu mẫu: **d1**, **d2**"
    )
    public ResponseEntity<OcrResultResponse> processOcr(
            @Parameter(description = "ID tài liệu cần xử lý OCR", example = "d1")
            @PathVariable("id") String id) {
        OcrResultResponse response = ocrService.processOcr(id);
        return ResponseEntity.ok(response);
    }
}
