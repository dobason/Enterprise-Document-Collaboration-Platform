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
@Table(name = "shares")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "document_id", length = 64, nullable = false)
    private String documentId;

    @Column(name = "shared_by", length = 64, nullable = false)
    private String sharedBy;

    @Column(name = "shared_with_email", length = 255, nullable = false)
    private String sharedWithEmail;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(length = 512, nullable = false, unique = true)
    private String token;

    @Column(name = "created_at")
    private Instant createdAt;
}
