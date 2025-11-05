package com.youthconnect.auth_service.controller;

import com.youthconnect.auth_service.dto.request.PasswordResetRequest;
import com.youthconnect.auth_service.dto.request.PasswordResetRequestDto;
import com.youthconnect.auth_service.dto.response.ApiResponse;
import com.youthconnect.auth_service.service.PasswordResetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================================
 *  PASSWORD RESET CONTROLLER
 * ============================================================================
 * Manages the secure password reset flow:
 *
 * 1. User requests password reset via email
 * 2. System generates a secure, time-limited token
 * 3. User receives reset link via email
 * 4. User submits new password using token
 * 5. System validates token and updates password
 *
 * SECURITY FEATURES:
 * - Tokens expire after a configurable duration (default 15 minutes)
 * - Tokens are single-use only
 * - Prevents email enumeration (always returns success)
 * - Audit logging for reset events
 *
 * @author
 *     Douglas Kings Kato
 * @version
 *     2.0.0 (Refined Edition)
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
@Tag(name = "Password Reset", description = "Password reset and recovery APIs")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    // =========================================================================
    // 1️⃣  REQUEST PASSWORD RESET
    // =========================================================================
    /**
     * Initiates the password reset process by sending a secure token to user's email.
     *
     * SECURITY NOTE:
     * Always returns success to prevent revealing if the email exists in the system.
     *
     * @param request DTO containing user's email address
     * @return Generic success response
     */
    @PostMapping("/forgot")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Request Password Reset",
            description = "Sends password reset email if account exists. Always returns success for security."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Request processed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid email format")
    })
    public ApiResponse<Void> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDto request) {

        log.info("Password reset requested for email: {}", maskEmail(request.getEmail()));

        passwordResetService.initiatePasswordReset(request.getEmail());

        return ApiResponse.success(
                null,
                "If your email is registered, you will receive a password reset link shortly."
        );
    }

    // =========================================================================
    // 2️⃣  VALIDATE RESET TOKEN
    // =========================================================================
    /**
     * Validates whether a password reset token is valid and unexpired.
     *
     * This endpoint is typically called before displaying the "Reset Password" form
     * in a frontend or mobile app.
     *
     * @param token The reset token provided in the email link
     * @return Boolean response indicating validity
     */
    @GetMapping("/validate-reset-token")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Validate Reset Token",
            description = "Checks if password reset token is valid and not expired"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token validation complete"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Token is missing or invalid format")
    })
    public ApiResponse<Boolean> validateResetToken(
            @RequestParam("token") String token) {

        log.debug("Validating password reset token");

        boolean isValid = passwordResetService.validateResetToken(token);

        return ApiResponse.success(
                isValid,
                isValid ? "Token is valid" : "Token is invalid or expired"
        );
    }

    // =========================================================================
    // 3️⃣  RESET PASSWORD
    // =========================================================================
    /**
     * Completes the password reset process by setting a new password
     * after token validation.
     *
     * @param request DTO containing reset token and new password
     * @return Success message
     */
    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Reset Password",
            description = "Sets new password using a valid reset token"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or password requirements not met"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    public ApiResponse<Void> resetPassword(
            @Valid @RequestBody PasswordResetRequest request) {

        log.info("Password reset attempt using token");

        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

        return ApiResponse.success(
                null,
                "Password has been reset successfully. You can now log in with your new password."
        );
    }

    // =========================================================================
    // PRIVATE UTILITIES
    // =========================================================================

    /**
     * Mask email for privacy in logs.
     * Example: john@example.com → joh***@example.com
     *
     * @param email The email address to mask
     * @return Masked email string
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
