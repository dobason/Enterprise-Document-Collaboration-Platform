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
public class ApprovalHistoryDto {
    private String id;
    private String documentId;
    private String action;
    private String fromStatus;
    private String toStatus;
    private String performedBy;
    private Instant timestamp;
}
