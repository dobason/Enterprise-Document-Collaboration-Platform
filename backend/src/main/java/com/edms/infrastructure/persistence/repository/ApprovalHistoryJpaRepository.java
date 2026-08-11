package com.edms.infrastructure.persistence.repository;

import com.edms.infrastructure.persistence.entity.ApprovalHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalHistoryJpaRepository extends JpaRepository<ApprovalHistoryEntity, String> {
    List<ApprovalHistoryEntity> findByDocumentIdOrderByTimestampAsc(String documentId);
}
