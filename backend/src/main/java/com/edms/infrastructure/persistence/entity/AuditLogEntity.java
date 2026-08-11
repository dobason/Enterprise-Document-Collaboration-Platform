package com.edms.infrastructure.persistence.entity;

import com.edms.domain.enums.AuditAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "document_id", length = 64)
    private String documentId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 50, nullable = false)
    private AuditAction action;

    @Column(name = "performed_by", length = 64, nullable = false)
    private String performedBy;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "timestamp")
    private Instant timestamp;
}
