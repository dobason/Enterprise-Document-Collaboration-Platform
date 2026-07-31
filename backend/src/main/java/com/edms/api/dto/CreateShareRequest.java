package com.edms.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShareRequest {

    @NotBlank(message = "email is required")
    private String email;

    @Positive(message = "ttlHours must be positive")
    private int ttlHours;
}
