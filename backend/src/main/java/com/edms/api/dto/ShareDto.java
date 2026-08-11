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
public class ShareDto {
    private String id;
    private String documentId;
    private String sharedWithEmail;
    private Instant expiresAt;
    private String link;
}
