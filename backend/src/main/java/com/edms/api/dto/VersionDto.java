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
public class VersionDto {
    private String id;
    private String documentId;
    private Integer versionNumber;
    private String content;
    private String createdBy;
    private Instant createdAt;
}
