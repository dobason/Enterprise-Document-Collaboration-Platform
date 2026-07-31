package com.edms.infrastructure.persistence.repository;

import com.edms.infrastructure.persistence.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagJpaRepository extends JpaRepository<TagEntity, String> {
    Optional<TagEntity> findByName(String name);
    boolean existsByName(String name);
}
