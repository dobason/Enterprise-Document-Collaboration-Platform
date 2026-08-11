package com.edms.infrastructure.persistence.repository;

import com.edms.infrastructure.persistence.entity.DocumentTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentTagJpaRepository extends JpaRepository<DocumentTagEntity, String> {
    List<DocumentTagEntity> findByDocumentId(String documentId);

    @Query("SELECT dt FROM DocumentTagEntity dt WHERE dt.documentId = :documentId AND dt.tagId = :tagId")
    Optional<DocumentTagEntity> findByDocumentIdAndTagId(@Param("documentId") String documentId,
                                                         @Param("tagId") String tagId);
}
