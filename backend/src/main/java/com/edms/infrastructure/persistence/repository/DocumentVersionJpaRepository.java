package com.edms.infrastructure.persistence.repository;

import com.edms.infrastructure.persistence.entity.DocumentVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentVersionJpaRepository extends JpaRepository<DocumentVersionEntity, String> {
    List<DocumentVersionEntity> findByDocumentIdOrderByVersionNumberDesc(String documentId);
    Optional<DocumentVersionEntity> findByDocumentIdAndId(String documentId, String id);

    @Query("SELECT MAX(v.versionNumber) FROM DocumentVersionEntity v WHERE v.documentId = :documentId")
    Optional<Integer> findMaxVersionNumberByDocumentId(@Param("documentId") String documentId);
}
