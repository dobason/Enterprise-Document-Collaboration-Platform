package com.edms.infrastructure.adapters.local;

import com.edms.api.dto.OcrResultResponse;
import com.edms.application.ports.OcrService;
import com.edms.domain.enums.OcrStatus;
import com.edms.infrastructure.persistence.entity.OcrResultEntity;
import com.edms.infrastructure.persistence.repository.OcrResultJpaRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Profile({"mysql", "aws"})
public class LocalOcrService implements OcrService {

    private final OcrResultJpaRepository ocrRepository;

    public LocalOcrService(OcrResultJpaRepository ocrRepository) {
        this.ocrRepository = ocrRepository;
    }

    @Override
    @Transactional
    public OcrResultResponse processOcr(String documentId, String s3Key) {
        Optional<OcrResultEntity> existing = ocrRepository.findByDocumentId(documentId);

        OcrResultEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setStatus(OcrStatus.COMPLETED);
            entity.setText("Extracted mock OCR text for document " + documentId);
            entity.setExtractedAt(Instant.now());
        } else {
            entity = OcrResultEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .documentId(documentId)
                    .status(OcrStatus.COMPLETED)
                    .text("Extracted mock OCR text for document " + documentId)
                    .extractedAt(Instant.now())
                    .build();
        }

        ocrRepository.save(entity);

        return OcrResultResponse.builder()
                .status(entity.getStatus().getValue())
                .text(entity.getText())
                .extractedAt(entity.getExtractedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OcrResultResponse getOcrResult(String documentId) {
        return ocrRepository.findByDocumentId(documentId)
                .map(entity -> OcrResultResponse.builder()
                        .status(entity.getStatus().getValue())
                        .text(entity.getText())
                        .extractedAt(entity.getExtractedAt())
                        .build())
                .orElseGet(() -> OcrResultResponse.builder()
                        .status(OcrStatus.NOT_FOUND.getValue())
                        .text(null)
                        .extractedAt(null)
                        .build());
    }
}
