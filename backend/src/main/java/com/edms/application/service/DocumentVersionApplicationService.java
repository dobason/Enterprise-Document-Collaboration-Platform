package com.edms.application.service;

import com.edms.api.dto.CreateVersionRequest;
import com.edms.api.dto.RollbackVersionRequest;
import com.edms.api.dto.VersionDto;
import com.edms.api.dto.VersionListResponse;
import com.edms.api.exception.ResourceNotFoundException;
import com.edms.infrastructure.persistence.entity.DocumentEntity;
import com.edms.infrastructure.persistence.entity.DocumentVersionEntity;
import com.edms.infrastructure.persistence.repository.DocumentJpaRepository;
import com.edms.infrastructure.persistence.repository.DocumentVersionJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentVersionApplicationService {

    private final DocumentJpaRepository documentRepository;
    private final DocumentVersionJpaRepository versionRepository;

    public DocumentVersionApplicationService(DocumentJpaRepository documentRepository,
                                             DocumentVersionJpaRepository versionRepository) {
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
    }

    @Transactional(readOnly = true)
    public VersionListResponse getVersions(String documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found: " + documentId);
        }
        List<DocumentVersionEntity> versions = versionRepository.findByDocumentIdOrderByVersionNumberDesc(documentId);
        List<VersionDto> dtos = versions.stream().map(this::mapToDto).collect(Collectors.toList());

        return VersionListResponse.builder()
                .items(dtos)
                .total(dtos.size())
                .build();
    }

    @Transactional
    public VersionDto createVersion(String documentId, CreateVersionRequest request, String currentUserId) {
        DocumentEntity doc = documentRepository.findByIdAndDeletedAtIsNull(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        int nextVersionNumber = versionRepository.findMaxVersionNumberByDocumentId(documentId).orElse(0) + 1;
        String creator = request.getCreatedBy() != null ? request.getCreatedBy() : currentUserId;

        DocumentVersionEntity version = DocumentVersionEntity.builder()
                .id(UUID.randomUUID().toString())
                .documentId(documentId)
                .versionNumber(nextVersionNumber)
                .content(request.getContent())
                .createdBy(creator != null ? creator : "u1")
                .createdAt(Instant.now())
                .build();

        DocumentVersionEntity savedVersion = versionRepository.save(version);

        // Update document current version & content
        doc.setCurrentVersionId(savedVersion.getId());
        if (request.getContent() != null) {
            doc.setContent(request.getContent());
        }
        doc.setUpdatedAt(Instant.now());
        documentRepository.save(doc);

        return mapToDto(savedVersion);
    }

    @Transactional
    public VersionDto rollbackVersion(String documentId, RollbackVersionRequest request, String currentUserId) {
        DocumentEntity doc = documentRepository.findByIdAndDeletedAtIsNull(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + documentId));

        DocumentVersionEntity targetVersion = versionRepository.findByDocumentIdAndId(documentId, request.getVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("Version not found: " + request.getVersionId()));

        int nextVersionNumber = versionRepository.findMaxVersionNumberByDocumentId(documentId).orElse(0) + 1;

        DocumentVersionEntity newRollbackVersion = DocumentVersionEntity.builder()
                .id(UUID.randomUUID().toString())
                .documentId(documentId)
                .versionNumber(nextVersionNumber)
                .content(targetVersion.getContent())
                .createdBy(currentUserId != null ? currentUserId : "u1")
                .createdAt(Instant.now())
                .build();

        DocumentVersionEntity savedNewVersion = versionRepository.save(newRollbackVersion);

        doc.setCurrentVersionId(savedNewVersion.getId());
        doc.setContent(targetVersion.getContent());
        doc.setUpdatedAt(Instant.now());
        documentRepository.save(doc);

        return mapToDto(savedNewVersion);
    }

    private VersionDto mapToDto(DocumentVersionEntity entity) {
        return VersionDto.builder()
                .id(entity.getId())
                .documentId(entity.getDocumentId())
                .versionNumber(entity.getVersionNumber())
                .content(entity.getContent())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
