package com.edms.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private String id;
    private String title;
    private String type;
    private String status;
    private String ownerId;
    private String folderId;
    private String content;
    private String currentVersionId;
    private Instant createdAt;
    private Instant updatedAt;
}
