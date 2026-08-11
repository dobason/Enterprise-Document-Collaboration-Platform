package com.edms.application.service;

import com.edms.api.dto.CreateFolderRequest;
import com.edms.api.dto.FolderDto;
import com.edms.api.exception.ResourceNotFoundException;
import com.edms.infrastructure.persistence.entity.FolderEntity;
import com.edms.infrastructure.persistence.repository.FolderJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FolderApplicationService {

    private final FolderJpaRepository folderRepository;

    public FolderApplicationService(FolderJpaRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    @Transactional(readOnly = true)
    public List<FolderDto> getAllFolders() {
        return folderRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FolderDto getFolderById(String id) {
        FolderEntity entity = folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found: " + id));
        return mapToDto(entity);
    }

    @Transactional
    public FolderDto createFolder(CreateFolderRequest request, String currentUserId) {
        String owner = request.getOwnerId() != null ? request.getOwnerId() : currentUserId;
        FolderEntity entity = FolderEntity.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .department(request.getDepartment() != null ? request.getDepartment() : "General")
                .ownerId(owner != null ? owner : "system")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        FolderEntity saved = folderRepository.save(entity);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteFolder(String id) {
        if (!folderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Folder not found: " + id);
        }
        folderRepository.deleteById(id);
    }

    private FolderDto mapToDto(FolderEntity entity) {
        return FolderDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .department(entity.getDepartment())
                .ownerId(entity.getOwnerId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
