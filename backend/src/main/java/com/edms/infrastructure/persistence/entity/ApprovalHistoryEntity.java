package com.edms.infrastructure.persistence.entity;

import com.edms.domain.enums.ApprovalAction;
import com.edms.domain.enums.DocumentStatus;
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
@Table(name = "approval_histories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalHistoryEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "document_id", length = 64, nullable = false)
    private String documentId;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 50, nullable = false)
    private ApprovalAction action;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "from_status", length = 50, nullable = false)
    private DocumentStatus fromStatus;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "to_status", length = 50, nullable = false)
    private DocumentStatus toStatus;

    @Column(name = "performed_by", length = 64, nullable = false)
    private String performedBy;

    @Column(name = "timestamp")
    private Instant timestamp;
}
