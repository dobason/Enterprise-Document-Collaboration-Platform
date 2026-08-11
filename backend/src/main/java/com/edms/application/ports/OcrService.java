package com.edms.application.ports;

import com.edms.api.dto.OcrResultResponse;

public interface OcrService {
    OcrResultResponse processOcr(String documentId, String s3Key);
    OcrResultResponse getOcrResult(String documentId);
}
