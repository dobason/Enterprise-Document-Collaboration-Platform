package com.edms.infrastructure.persistence.entity;

import com.edms.domain.enums.DocumentStatus;
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
@Table(name = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(length = 100, nullable = false)
    private String type;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 50, nullable = false)
    private DocumentStatus status;

    @Column(name = "owner_id", length = 64, nullable = false)
    private String ownerId;

    @Column(name = "folder_id", length = 64)
    private String folderId;

    @Column(name = "department_id", length = 64)
    private String departmentId;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "s3_key", length = 512)
    private String s3Key;

    @Column(name = "current_version_id", length = 64)
    private String currentVersionId;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
