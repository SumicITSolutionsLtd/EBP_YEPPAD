package com.youthconnect.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Password Reset Request DTO
 *
 * Used when user submits new password with reset token.
 * Contains the token and new password.
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequest {

    /**
     * Password reset token from email link
     */
    @NotBlank(message = "Reset token is required")
    private String token;

    /**
     * New password to set
     * Must meet security requirements
     */
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    private String newPassword;

    /**
     * Password confirmation (optional but recommended)
     */
    private String confirmPassword;

    /**
     * Validate password confirmation matches
     */
    public boolean isPasswordConfirmed() {
        if (confirmPassword == null || confirmPassword.isBlank()) {
            return true; // Skip validation if not provided
        }
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}