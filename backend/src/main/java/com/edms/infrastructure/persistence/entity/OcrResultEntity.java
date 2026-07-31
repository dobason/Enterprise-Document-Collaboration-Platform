package com.edms.infrastructure.persistence.entity;

import com.edms.domain.enums.OcrStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "ocr_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrResultEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "document_id", length = 64, nullable = false, unique = true)
    private String documentId;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private OcrStatus status;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(name = "extracted_at")
    private Instant extractedAt;
}
