package com.edms.application.service;

import com.edms.api.dto.GrantPermissionRequest;
import com.edms.api.dto.PermissionDto;
import com.edms.api.dto.PermissionListResponse;
import com.edms.api.dto.UpdatePermissionRequest;
import com.edms.api.dto.UserRoleResponse;
import com.edms.api.exception.BadRequestException;
import com.edms.api.exception.ResourceNotFoundException;
import com.edms.domain.enums.PermissionRole;
import com.edms.infrastructure.persistence.entity.PermissionEntity;
import com.edms.infrastructure.persistence.entity.UserEntity;
import com.edms.infrastructure.persistence.repository.DocumentJpaRepository;
import com.edms.infrastructure.persistence.repository.PermissionJpaRepository;
import com.edms.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PermissionApplicationService {

    private final DocumentJpaRepository documentRepository;
    private final PermissionJpaRepository permissionRepository;
    private final UserJpaRepository userRepository;

    public PermissionApplicationService(DocumentJpaRepository documentRepository,
                                        PermissionJpaRepository permissionRepository,
                                        UserJpaRepository userRepository) {
        this.documentRepository = documentRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PermissionListResponse getPermissions(String documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found: " + documentId);
        }

        List<PermissionEntity> entities = permissionRepository.findByDocumentId(documentId);
        List<PermissionDto> dtos = new ArrayList<>();

        for (PermissionEntity p : entities) {
            Optional<UserEntity> userOpt = userRepository.findById(p.getUserId());
            dtos.add(PermissionDto.builder()
                    .id(p.getId())
                    .documentId(p.getDocumentId())
                    .userId(p.getUserId())
                    .role(p.getRole().name())
                    .userName(userOpt.map(UserEntity::getName).orElse("Unknown"))
                    .userEmail(userOpt.map(UserEntity::getEmail).orElse("Unknown"))
                    .build());
        }

        return PermissionListResponse.builder().items(dtos).build();
    }

    @Transactional
    public PermissionDto grantPermission(String documentId, GrantPermissionRequest request) {
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found: " + documentId);
        }

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

        PermissionRole role;
        try {
            role = PermissionRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid permission role: " + request.getRole());
        }

        // Upsert permission
        Optional<PermissionEntity> existing = permissionRepository.findByDocumentIdAndUserId(documentId, user.getId());
        PermissionEntity perm;
        if (existing.isPresent()) {
            perm = existing.get();
            perm.setRole(role);
        } else {
            perm = PermissionEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .documentId(documentId)
                    .userId(user.getId())
                    .role(role)
                    .createdAt(Instant.now())
                    .build();
        }

        PermissionEntity saved = permissionRepository.save(perm);

        return PermissionDto.builder()
                .id(saved.getId())
                .documentId(saved.getDocumentId())
                .userId(saved.getUserId())
                .role(saved.getRole().name())
                .userName(user.getName())
                .userEmail(user.getEmail())
                .build();
    }

    @Transactional
    public PermissionDto updatePermission(String documentId, String permissionId, UpdatePermissionRequest request) {
        PermissionEntity perm = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found: " + permissionId));

        PermissionRole role;
        try {
            role = PermissionRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid permission role: " + request.getRole());
        }

        perm.setRole(role);
        PermissionEntity updated = permissionRepository.save(perm);

        Optional<UserEntity> userOpt = userRepository.findById(updated.getUserId());

        return PermissionDto.builder()
                .id(updated.getId())
                .documentId(updated.getDocumentId())
                .userId(updated.getUserId())
                .role(updated.getRole().name())
                .userName(userOpt.map(UserEntity::getName).orElse("Unknown"))
                .userEmail(userOpt.map(UserEntity::getEmail).orElse("Unknown"))
                .build();
    }

    @Transactional
    public void revokePermission(String documentId, String permissionId) {
        if (!permissionRepository.existsById(permissionId)) {
            throw new ResourceNotFoundException("Permission not found: " + permissionId);
        }
        permissionRepository.deleteById(permissionId);
    }

    @Transactional(readOnly = true)
    public UserRoleResponse getUserRoleForDocument(String documentId, String userId) {
        Optional<PermissionEntity> permOpt = permissionRepository.findByDocumentIdAndUserId(documentId, userId);
        String role = permOpt.map(p -> p.getRole().name()).orElse("NONE");
        return UserRoleResponse.builder().role(role).build();
    }
}
