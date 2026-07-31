package com.edms.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "document_tags")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTagEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "document_id", length = 64, nullable = false)
    private String documentId;

    @Column(name = "tag_id", length = 64, nullable = false)
    private String tagId;

    @Column(name = "created_at")
    private Instant createdAt;
}
