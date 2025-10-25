package com.youthconnect.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Password Reset Request DTO
 *
 * Used when user requests a password reset link.
 * Contains only the email address.
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequestDto {

    /**
     * User's email address
     * Reset link will be sent to this email
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}