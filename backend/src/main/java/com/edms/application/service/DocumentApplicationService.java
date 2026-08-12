package com.edms.application.service;

import com.edms.api.dto.CreateDocumentRequest;
import com.edms.api.dto.DocumentDto;
import com.edms.api.dto.PageResponse;
import com.edms.api.dto.UpdateDocumentRequest;
import com.edms.api.exception.ForbiddenException;
import com.edms.api.exception.ResourceNotFoundException;
import com.edms.application.ports.AuditService;
import com.edms.application.ports.EventPublisher;
import com.edms.application.ports.StorageService;
import com.edms.domain.enums.AuditAction;
import com.edms.domain.enums.DocumentStatus;
import com.edms.domain.enums.PermissionRole;
import com.edms.domain.events.DocumentDeletedEvent;
import com.edms.domain.events.DocumentUploadedEvent;
import com.edms.infrastructure.persistence.entity.DocumentEntity;
import com.edms.infrastructure.persistence.entity.DocumentVersionEntity;
import com.edms.infrastructure.persistence.entity.PermissionEntity;
import com.edms.infrastructure.persistence.repository.DocumentJpaRepository;
import com.edms.infrastructure.persistence.repository.DocumentVersionJpaRepository;
import com.edms.infrastructure.persistence.repository.PermissionJpaRepository;
import com.edms.infrastructure.persistence.repository.FolderJpaRepository;
import com.edms.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentApplicationService {

    private final DocumentJpaRepository documentRepository;
    private final DocumentVersionJpaRepository versionRepository;
    private final PermissionJpaRepository permissionRepository;
    private final FolderJpaRepository folderRepository;
    private final UserJpaRepository userRepository;
    private final AuditService auditService;
    private final EventPublisher eventPublisher;
    private final StorageService storageService;

    public DocumentApplicationService(DocumentJpaRepository documentRepository,
            DocumentVersionJpaRepository versionRepository,
            PermissionJpaRepository permissionRepository,
            FolderJpaRepository folderRepository,
            UserJpaRepository userRepository,
            AuditService auditService,
            EventPublisher eventPublisher,
            StorageService storageService) {
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
        this.permissionRepository = permissionRepository;
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public PageResponse<DocumentDto> getDocuments(int page, int limit, String sortBy, String sortOrder,
            String currentUserId) {
        return getDocuments(page, limit, sortBy, sortOrder, null, currentUserId);
    }

    @Transactional(readOnly = true)
    public PageResponse<DocumentDto> getDocuments(int page, int limit, String sortBy, String sortOrder, String folderId,
            String currentUserId) {
        int pageNumber = page > 0 ? page - 1 : 0;
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String field = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";

        Pageable pageable = PageRequest.of(pageNumber, limit, Sort.by(direction, field));
        Page<DocumentEntity> pageResult = (folderId != null && !folderId.isBlank())
                ? documentRepository.findByDeletedAtIsNullAndFolderId(folderId, pageable)
                : documentRepository.findAllActive(pageable);

        List<DocumentDto> dtos = pageResult.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return PageResponse.<DocumentDto>builder()
                .items(dtos)
                .total(pageResult.getTotalElements())
                .page(pageNumber + 1)
                .limit(limit)
                .totalPages(pageResult.getTotalPages())
                .build();
    }

    @Transactional
    public DocumentDto getDocumentById(String id, String currentUserId) {
        DocumentEntity entity = documentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

        auditService.log(id, AuditAction.VIEW, currentUserId, "Viewed document");

        return mapToDto(entity);
    }

    @Transactional
    public DocumentDto createDocument(CreateDocumentRequest request, String currentUserId) {
        String docId = UUID.randomUUID().toString();
        String versionId = UUID.randomUUID().toString();

        String departmentId = null;
        if (request.getFolderId() != null && !request.getFolderId().isBlank()) {
            departmentId = folderRepository.findById(request.getFolderId())
                    .map(com.edms.infrastructure.persistence.entity.FolderEntity::getDepartmentId)
                    .orElse(null);
        }
        if (departmentId == null) {
            String ownerId = currentUserId != null ? currentUserId : "u1";
            departmentId = userRepository.findById(ownerId)
                    .map(com.edms.infrastructure.persistence.entity.UserEntity::getDepartmentId)
                    .orElse(null);
        }

        DocumentEntity entity = DocumentEntity.builder()
                .id(docId)
                .title(request.getTitle())
                .type(request.getType())
                .status(DocumentStatus.DRAFT)
                .ownerId(currentUserId != null ? currentUserId : "u1")
                .folderId(request.getFolderId())
                .departmentId(departmentId)
                .content(request.getContent())
                .fileName(request.getFileName())
                .fileType(request.getFileType())
                .s3Key(request.getS3Key())
                .currentVersionId(versionId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        DocumentEntity savedDoc = documentRepository.save(entity);

        // Auto create Version 1
        DocumentVersionEntity version = DocumentVersionEntity.builder()
                .id(versionId)
                .documentId(docId)
                .versionNumber(1)
                .content(request.getContent())
                .createdBy(savedDoc.getOwnerId())
                .createdAt(Instant.now())
                .build();
        versionRepository.save(version);

        // Auto grant OWNER permission
        PermissionEntity perm = PermissionEntity.builder()
                .id(UUID.randomUUID().toString())
                .documentId(docId)
                .userId(savedDoc.getOwnerId())
                .role(PermissionRole.OWNER)
                .createdAt(Instant.now())
                .build();
        permissionRepository.save(perm);

        auditService.log(docId, AuditAction.UPLOAD, currentUserId, "Created new document");
        eventPublisher.publish(new DocumentUploadedEvent(UUID.randomUUID().toString(), docId, currentUserId));

        return mapToDto(savedDoc);
    }

    @Transactional
    public DocumentDto updateDocument(String id, UpdateDocumentRequest request, String currentUserId) {
        DocumentEntity doc = documentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

        if (request.getTitle() != null) {
            doc.setTitle(request.getTitle());
        }
        if (request.getFolderId() != null) {
            doc.setFolderId(request.getFolderId());
        }
        if (request.getContent() != null) {
            doc.setContent(request.getContent());
        }
        doc.setUpdatedAt(Instant.now());

        DocumentEntity updated = documentRepository.save(doc);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteDocument(String id, String currentUserId) {
        DocumentEntity doc = documentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

        doc.setDeletedAt(Instant.now());
        documentRepository.save(doc);

        auditService.log(id, AuditAction.DELETE, currentUserId, "Soft deleted document");
        eventPublisher.publish(new DocumentDeletedEvent(UUID.randomUUID().toString(), id, currentUserId));
    }

    @Transactional(readOnly = true)
    public FileDownload downloadDocument(String id, String currentUserId) {
        DocumentEntity entity = documentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));

        auditService.log(id, AuditAction.DOWNLOAD, currentUserId, "Downloaded document");

        String key = entity.getS3Key() != null ? entity.getS3Key() : entity.getId();
        byte[] content = storageService.downloadFile(key);
        String fileName = entity.getFileName() != null ? entity.getFileName() : entity.getTitle();
        return new FileDownload(content, fileName, entity.getFileType());
    }

    public record FileDownload(byte[] content, String fileName, String contentType) {
    }

    public DocumentDto mapToDto(DocumentEntity entity) {
        return DocumentDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .type(entity.getType())
                .status(entity.getStatus().name())
                .ownerId(entity.getOwnerId())
                .folderId(entity.getFolderId())
                .content(entity.getContent())
                .currentVersionId(entity.getCurrentVersionId())
                .fileName(entity.getFileName())
                .fileType(entity.getFileType())
                .s3Key(entity.getS3Key())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
