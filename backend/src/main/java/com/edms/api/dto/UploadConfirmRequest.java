package com.edms.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadConfirmRequest {

    @NotBlank(message = "fileId is required")
    private String fileId;

    @NotBlank(message = "fileName is required")
    private String fileName;

    @NotBlank(message = "fileType is required")
    private String fileType;

    private String ownerId;
    
    private String folderId;
    
    private String extractedContent;
}
