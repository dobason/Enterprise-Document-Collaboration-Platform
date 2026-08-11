package com.edms.infrastructure.persistence.repository;

import com.edms.domain.enums.PermissionRole;
import com.edms.infrastructure.persistence.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionJpaRepository extends JpaRepository<PermissionEntity, String> {
    List<PermissionEntity> findByDocumentId(String documentId);
    Optional<PermissionEntity> findByDocumentIdAndUserId(String documentId, String userId);
    boolean existsByDocumentIdAndUserIdAndRole(String documentId, String userId, PermissionRole role);
}
