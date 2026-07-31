package com.edms.infrastructure.persistence.repository;

import com.edms.infrastructure.persistence.entity.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderJpaRepository extends JpaRepository<FolderEntity, String> {
}
