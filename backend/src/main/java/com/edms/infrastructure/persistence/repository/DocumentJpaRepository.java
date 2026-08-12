package com.edms.infrastructure.persistence.repository;

import com.edms.domain.enums.DocumentStatus;
import com.edms.infrastructure.persistence.entity.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentJpaRepository extends JpaRepository<DocumentEntity, String>,
        JpaSpecificationExecutor<DocumentEntity> {

    @Query("SELECT d FROM DocumentEntity d WHERE d.deletedAt IS NULL")
    List<DocumentEntity> findAllActive();

    @Query("SELECT d FROM DocumentEntity d WHERE d.deletedAt IS NULL")
    Page<DocumentEntity> findAllActive(Pageable pageable);

    Page<DocumentEntity> findByDeletedAtIsNullAndFolderId(String folderId, Pageable pageable);

    @Query("SELECT d FROM DocumentEntity d WHERE d.deletedAt IS NULL AND (:isAdmin = true OR EXISTS (" +
           "SELECT 1 FROM PermissionEntity p WHERE p.documentId = d.id AND p.userId = :userId))")
    Page<DocumentEntity> findAllActiveForUser(@Param("isAdmin") boolean isAdmin, @Param("userId") String userId, Pageable pageable);

    @Query("SELECT d FROM DocumentEntity d WHERE d.deletedAt IS NULL AND d.folderId = :folderId AND (:isAdmin = true OR EXISTS (" +
           "SELECT 1 FROM PermissionEntity p WHERE p.documentId = d.id AND p.userId = :userId))")
    Page<DocumentEntity> findByFolderIdForUser(@Param("folderId") String folderId, @Param("isAdmin") boolean isAdmin, @Param("userId") String userId, Pageable pageable);

    @Query("SELECT d FROM DocumentEntity d WHERE d.deletedAt IS NULL AND EXISTS (" +
           "SELECT 1 FROM PermissionEntity p WHERE p.documentId = d.id AND p.userId = :userId)")
    List<DocumentEntity> findAllSharedWithUser(@Param("userId") String userId);

    Optional<DocumentEntity> findByIdAndDeletedAtIsNull(String id);

    @Query("SELECT COUNT(d) FROM DocumentEntity d WHERE d.deletedAt IS NULL AND d.status = :status")
    long countByStatusAndDeletedAtIsNull(@Param("status") DocumentStatus status);

    @Query("SELECT COUNT(d) FROM DocumentEntity d WHERE d.deletedAt IS NULL AND d.status = 'APPROVED' AND d.updatedAt >= :startOfMonth")
    long countApprovedThisMonth(@Param("startOfMonth") Instant startOfMonth);

    @Query("SELECT dp.name, COUNT(d) FROM DocumentEntity d " +
           "LEFT JOIN DepartmentEntity dp ON d.departmentId = dp.id " +
           "WHERE d.deletedAt IS NULL " +
           "GROUP BY dp.name")
    List<Object[]> countDocumentsByDepartmentRaw();
}
