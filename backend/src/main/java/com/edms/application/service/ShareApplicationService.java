package com.edms.application.service;

import com.edms.api.dto.CreateShareRequest;
import com.edms.api.dto.ShareDto;
import com.edms.api.dto.ShareLinkResponse;
import com.edms.api.dto.ShareListResponse;
import com.edms.api.exception.ResourceNotFoundException;
import com.edms.application.ports.AuditService;
import com.edms.domain.enums.AuditAction;
import com.edms.infrastructure.persistence.entity.ShareEntity;
import com.edms.infrastructure.persistence.repository.DocumentJpaRepository;
import com.edms.infrastructure.persistence.repository.ShareJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShareApplicationService {

    private final DocumentJpaRepository documentRepository;
    private final ShareJpaRepository shareRepository;
    private final AuditService auditService;

    public ShareApplicationService(DocumentJpaRepository documentRepository,
                                   ShareJpaRepository shareRepository,
                                   AuditService auditService) {
        this.documentRepository = documentRepository;
        this.shareRepository = shareRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ShareLinkResponse createShareLink(String documentId, CreateShareRequest request, String currentUserId) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found: " + documentId);
        }

        String token = UUID.randomUUID().toString();
        int hours = request.getTtlHours() > 0 ? request.getTtlHours() : 24;
        Instant expiresAt = Instant.now().plus(hours, ChronoUnit.HOURS);
        String sharer = currentUserId != null ? currentUserId : "u1";

        ShareEntity share = ShareEntity.builder()
                .id(UUID.randomUUID().toString())
                .documentId(documentId)
                .sharedBy(sharer)
                .sharedWithEmail(request.getEmail())
                .expiresAt(expiresAt)
                .token(token)
                .createdAt(Instant.now())
                .build();

        shareRepository.save(share);

        String link = "http://localhost:8080/share/" + token;
        auditService.log(documentId, AuditAction.SHARE, currentUserId, "Shared document with " + request.getEmail());

        return ShareLinkResponse.builder().link(link).build();
    }

    @Transactional(readOnly = true)
    public ShareDto getShareByDocumentId(String documentId) {
        List<ShareEntity> list = shareRepository.findByDocumentId(documentId);
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("No active share link found for document: " + documentId);
        }
        ShareEntity share = list.get(list.size() - 1);
        return mapToDto(share);
    }

    @Transactional(readOnly = true)
    public ShareListResponse getDocumentShares(String documentId) {
        List<ShareEntity> list = shareRepository.findByDocumentId(documentId);
        List<ShareDto> dtos = list.stream().map(this::mapToDto).collect(Collectors.toList());
        return ShareListResponse.builder().items(dtos).build();
    }

    private ShareDto mapToDto(ShareEntity entity) {
        return ShareDto.builder()
                .id(entity.getId())
                .documentId(entity.getDocumentId())
                .sharedWithEmail(entity.getSharedWithEmail())
                .expiresAt(entity.getExpiresAt())
                .link("http://localhost:8080/share/" + entity.getToken())
                .build();
    }
}
