package com.edms.infrastructure.persistence.entity;

import com.edms.domain.enums.PermissionRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "document_id", length = 64, nullable = false)
    private String documentId;

    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 50, nullable = false)
    private PermissionRole role;

    @Column(name = "created_at")
    private Instant createdAt;
}
