package com.youthconnect.auth_service.controller;

import com.youthconnect.auth_service.dto.request.PasswordResetRequest;
import com.youthconnect.auth_service.dto.request.PasswordResetRequestDto;
import com.youthconnect.auth_service.dto.response.ApiResponse;
import com.youthconnect.auth_service.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Password Reset Controller
 *
 * Handles password reset workflow:
 * 1. User requests password reset (provides email)
 * 2. System generates secure token and sends email
 * 3. User clicks link with token
 * 4. User submits new password with token
 * 5. System validates token and updates password
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/password")
@RequiredArgsConstructor
@Tag(name = "Password Reset", description = "Password reset and recovery APIs")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Request Password Reset
     *
     * Initiates password reset process:
     * - Generates secure token
     * - Stores token in database with expiration
     * - Sends email with reset link
     *
     * Endpoint: {@code POST /api/auth/password/forgot}
     *
     * @param request Request containing user email
     * @return Success message (always returns success for security)
     */
    @PostMapping("/forgot")
    @Operation(
            summary = "Request Password Reset",
            description = "Sends password reset email if account exists"
    )
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDto request) {

        log.info("Password reset requested for email: {}", maskEmail(request.getEmail()));

        passwordResetService.initiatePasswordReset(request.getEmail());

        // Always return success (don't reveal if email exists)
        return ResponseEntity.ok(ApiResponse.success(
                null,
                "If your email is registered, you will receive a password reset link."
        ));
    }

    /**
     * Validate Reset Token
     *
     * Checks if password reset token is valid and not expired.
     * Called before showing password reset form.
     *
     * Endpoint: {@code GET /api/auth/password/validate-reset-token?token={token}}
     *
     * @param token Reset token from email link
     * @return Validation result
     */
    @GetMapping("/validate-reset-token")
    @Operation(
            summary = "Validate Reset Token",
            description = "Checks if password reset token is valid"
    )
    public ResponseEntity<ApiResponse<Boolean>> validateResetToken(
            @RequestParam("token") String token) {

        log.debug("Validating password reset token");

        boolean isValid = passwordResetService.validateResetToken(token);

        return ResponseEntity.ok(ApiResponse.success(
                isValid,
                isValid ? "Token is valid" : "Token is invalid or expired"
        ));
    }

    /**
     * Reset Password
     *
     * Completes password reset with new password.
     *
     * Endpoint: {@code POST /api/auth/password/reset}
     *
     * @param request Request containing token and new password
     * @return Success or error message
     */
    @PostMapping("/reset")
    @Operation(
            summary = "Reset Password",
            description = "Sets new password using reset token"
    )
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request) {

        log.info("Password reset attempt with token");

        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

        return ResponseEntity.ok(ApiResponse.success(
                null,
                "Password has been reset successfully. You can now login with your new password."
        ));
    }

    /**
     * Mask email for privacy in logs
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        if (parts[0].length() <= 3) {
            return "***@" + parts[1];
        }
        return parts[0].substring(0, 3) + "***@" + parts[1];
    }
}