package com.edms.infrastructure.persistence.repository;

import com.edms.infrastructure.persistence.entity.DepartmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentJpaRepository extends JpaRepository<DepartmentEntity, String> {
    Optional<DepartmentEntity> findByCode(String code);
}
