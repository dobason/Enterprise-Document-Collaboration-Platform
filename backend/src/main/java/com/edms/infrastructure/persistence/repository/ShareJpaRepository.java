package com.edms.infrastructure.persistence.repository;

import com.edms.infrastructure.persistence.entity.ShareEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShareJpaRepository extends JpaRepository<ShareEntity, String> {
    List<ShareEntity> findByDocumentId(String documentId);
    Optional<ShareEntity> findByToken(String token);
}
