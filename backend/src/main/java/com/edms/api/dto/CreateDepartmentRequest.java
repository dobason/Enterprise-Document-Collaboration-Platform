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
public class CreateDepartmentRequest {
    @NotBlank(message = "Department code is required")
    private String code;
    
    @NotBlank(message = "Department name is required")
    private String name;
}
