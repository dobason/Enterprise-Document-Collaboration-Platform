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
public class MyDocumentDto {
    private String id;
    private String title;
    private String status;
    private String folderId;
    private String role;
    private Instant createdAt;
    private Instant updatedAt;
}
