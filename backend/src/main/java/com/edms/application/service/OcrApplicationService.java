package com.edms.application.service;

import com.edms.api.dto.OcrResultResponse;
import com.edms.api.exception.ResourceNotFoundException;
import com.edms.application.ports.OcrService;
import com.edms.infrastructure.persistence.entity.DocumentEntity;
import com.edms.infrastructure.persistence.repository.DocumentJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class OcrApplicationService {

    private final OcrService ocrService;
    private final DocumentJpaRepository documentRepository;

    public OcrApplicationService(OcrService ocrService, DocumentJpaRepository documentRepository) {
        this.ocrService = ocrService;
        this.documentRepository = documentRepository;
    }

    public OcrResultResponse processOcr(String documentId) {
        DocumentEntity doc = documentRepository.findByIdAndDeletedAtIsNull(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        return ocrService.processOcr(documentId, doc.getS3Key());
    }

    public OcrResultResponse getOcrResult(String documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found: " + documentId);
        }
        return ocrService.getOcrResult(documentId);
    }
}
