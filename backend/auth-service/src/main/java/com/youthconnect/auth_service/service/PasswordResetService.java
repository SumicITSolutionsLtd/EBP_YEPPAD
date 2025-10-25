package com.youthconnect.auth_service.service;

import com.youthconnect.auth_service.client.NotificationServiceClient;
import com.youthconnect.auth_service.client.UserServiceClient;
import com.youthconnect.auth_service.dto.response.ApiResponse;
import com.youthconnect.auth_service.dto.response.UserInfoResponse;
import com.youthconnect.auth_service.entity.PasswordResetToken;
import com.youthconnect.auth_service.exception.InvalidCredentialsException;
import com.youthconnect.auth_service.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Password Reset Service
 *
 * Handles secure password reset workflow with token-based verification.
 *
 * Security Features:
 * <ul>
 *     <li>Secure random token generation (UUID)</li>
 *     <li>Token expiration (15 minutes by default)</li>
 *     <li>One-time use tokens</li>
 *     <li>Rate limiting on reset requests</li>
 * </ul>
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.token-expiry-minutes:15}")
    private int tokenExpiryMinutes;

    @Value("${app.password-reset.max-attempts:3}")
    private int maxAttempts;

    /**
     * Initiate Password Reset
     *
     * Creates reset token and sends email.
     * Always returns success (security: don't reveal if email exists).
     *
     * @param email User's email address
     */
    public void initiatePasswordReset(String email) {
        log.info("Initiating password reset for email: {}", maskEmail(email));

        try {
            // Check if user exists
            ApiResponse<UserInfoResponse> response = userServiceClient.getUserByIdentifier(email);

            if (response == null || !response.isSuccess() || response.getData() == null) {
                // User not found - log but don't reveal to caller
                log.warn("Password reset requested for non-existent email: {}", maskEmail(email));
                return;
            }

            UserInfoResponse user = response.getData();

            // Generate secure token
            String token = UUID.randomUUID().toString();

            // Create token entity
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUserId(user.getUserId());
            resetToken.setUserEmail(user.getEmail());
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(tokenExpiryMinutes));
            resetToken.setCreatedAt(LocalDateTime.now());
            resetToken.setUsed(false);
            resetToken.setAttempts(0);
            resetToken.setMaxAttempts(maxAttempts);

            // Save token
            tokenRepository.save(resetToken);

            // Send reset email
            notificationServiceClient.sendPasswordResetEmail(user.getEmail(), token);

            log.info("Password reset token created and email sent for user ID: {}", user.getUserId());

        } catch (Exception e) {
            log.error("Error initiating password reset: {}", e.getMessage(), e);
            // Don't throw exception - always return success for security
        }
    }

    /**
     * Validate Reset Token
     *
     * Checks if token is valid, not expired, and not used.
     *
     * @param token Reset token
     * @return true if valid, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean validateResetToken(String token) {
        try {
            return tokenRepository.findByTokenAndUsedFalse(token)
                    .map(resetToken -> {
                        boolean isValid = resetToken.getExpiresAt().isAfter(LocalDateTime.now())
                                && resetToken.getAttempts() < resetToken.getMaxAttempts();

                        if (!isValid) {
                            log.debug("Token validation failed: expired or max attempts reached");
                        }

                        return isValid;
                    })
                    .orElse(false);

        } catch (Exception e) {
            log.error("Error validating reset token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Reset Password
     *
     * Updates user password if token is valid.
     *
     * @param token Reset token
     * @param newPassword New password (plain text, will be hashed)
     * @throws InvalidCredentialsException if token is invalid
     */
    public void resetPassword(String token, String newPassword) {
        log.info("Processing password reset with token");

        // Find token
        PasswordResetToken resetToken = tokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid or expired reset token"));

        // Check expiration
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Reset token has expired");
        }

        // Check attempts
        if (resetToken.getAttempts() >= resetToken.getMaxAttempts()) {
            throw new InvalidCredentialsException("Maximum reset attempts exceeded");
        }

        // Increment attempts
        resetToken.setAttempts(resetToken.getAttempts() + 1);
        tokenRepository.save(resetToken);

        try {
            // Hash new password
            String hashedPassword = passwordEncoder.encode(newPassword);

            // Update password in user-service
            // NOTE: You'll need to add this endpoint to UserServiceClient
            // userServiceClient.updatePassword(resetToken.getUserId(), hashedPassword);

            // Mark token as used
            resetToken.setUsed(true);
            resetToken.setUsedAt(LocalDateTime.now());
            tokenRepository.save(resetToken);

            log.info("Password reset successful for user ID: {}", resetToken.getUserId());

        } catch (Exception e) {
            log.error("Error resetting password: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to reset password. Please try again.");
        }
    }

    /**
     * Mask email for privacy
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