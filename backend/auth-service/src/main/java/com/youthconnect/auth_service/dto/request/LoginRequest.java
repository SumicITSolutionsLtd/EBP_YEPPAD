package com.youthconnect.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email or phone number is required")
    private String identifier; // Can be email or phone

    @NotBlank(message = "Password is required")
    private String password;
}
