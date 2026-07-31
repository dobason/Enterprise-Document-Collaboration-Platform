package com.edms.infrastructure.persistence.repository;

import com.edms.infrastructure.persistence.entity.OcrResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OcrResultJpaRepository extends JpaRepository<OcrResultEntity, String> {
    Optional<OcrResultEntity> findByDocumentId(String documentId);
}
