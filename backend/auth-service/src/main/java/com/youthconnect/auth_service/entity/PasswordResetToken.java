package com.youthconnect.auth_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * Password Reset Token Entity
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Stores password reset tokens with expiration and usage tracking.
 * Tokens are designed to be single-use and expire after a configurable time
 * (defaulting to 15 minutes) to enhance security.
 *
 * SECURITY FEATURES:
 * ✅ Secure random token (UUID format) to prevent guessing.
 * ✅ Short expiration time (15 minutes default) to limit attack window.
 * ✅ One-time use only to prevent replay attacks.
 * ✅ Attempt tracking to prevent brute-force attacks on the token itself.
 * ✅ Maximum attempts limit with automatic lockout for increased security.
 * ✅ IP and user agent logging for an audit trail and suspicious activity detection.
 * ✅ Comprehensive validation methods for robust token checking.
 *
 * DATABASE TABLE: `password_reset_tokens`
 *
 * USAGE EXAMPLE:
 * ```java
 * // Create new token
 * PasswordResetToken token = PasswordResetToken.createToken(
 *     userId,
 *     "user@example.com",
 *     15  // expires in 15 minutes
 * );
 * tokenRepository.save(token); // Persist the token
 *
 * // ... later, when user tries to reset password ...
 * // Retrieve token from repository by token string
 * PasswordResetToken foundToken = tokenRepository.findByToken(tokenString).orElseThrow(...);
 *
 * // Check validity before processing
 * if (foundToken.isValid()) {
 *     foundToken.incrementAttempts(); // Always increment on attempt, even if password fails
 *     if (passwordService.validate(newPassword)) { // Assume password validation logic
 *         foundToken.markAsUsed();
 *         passwordService.reset(userId, newPassword);
 *         // Send success notification
 *     } else {
 *         // Password validation failed, provide feedback
 *     }
 *     tokenRepository.save(foundToken); // Save updated token state
 * } else {
 *     // Token is invalid, get specific error
 *     String errorMessage = foundToken.getValidationError();
 *     // Throw exception or return error message to user
 * }
 * ```
 *
 * @author Douglas Kings Kato
 * @version 3.0.0 (Complete - Added attempts, maxAttempts, and all validations)
 * @since 2025-01-15
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
        @Index(name = "idx_password_reset_token", columnList = "token"),
        @Index(name = "idx_password_reset_user", columnList = "user_id"),
        @Index(name = "idx_password_reset_expires", columnList = "expires_at"),
        @Index(name = "idx_password_reset_used", columnList = "used"),
        @Index(name = "idx_password_reset_attempts", columnList = "attempts, used") // For efficient lockout checks
})
@Data // Lombok annotation to generate getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
@AllArgsConstructor // Lombok annotation to generate a constructor with all fields
public class PasswordResetToken {

    // ═══════════════════════════════════════════════════════════════════════
    // PRIMARY KEY
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Primary Key - UUID.
     * Automatically generated using Hibernate's UUID generator strategy.
     * The `updatable = false` prevents modification of the ID after creation.
     * `nullable = false` ensures every entity has an ID.
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID id;

    // ═══════════════════════════════════════════════════════════════════════
    // TOKEN INFORMATION
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Secure random token (UUID format) that is sent to the user via email in the reset link.
     * Must be unique and indexed for fast lookup.
     *
     * SECURITY: The token is cryptographically random and unpredictable, making it hard to guess.
     */
    @Column(nullable = false, unique = true, length = 255)
    private String token;

    /**
     * Number of times this token has been attempted.
     * Incremented on each reset attempt (whether successful or failed).
     *
     * SECURITY BENEFIT:
     * - Prevents brute-force token reuse attempts.
     * - Limits password guessing attempts for a given token.
     * - Provides an audit trail of suspicious activity.
     */
    @Column(name = "attempts", nullable = false)
    private Integer attempts; // Initialized in @PrePersist for consistency

    /**
     * Maximum allowed attempts before the token is considered locked out.
     * Default value: 3 attempts.
     *
     * SECURITY RATIONALE:
     * - 3 attempts balances security and user experience.
     * - Effectively prevents brute-force password attacks using a single token.
     * - Forces the user to request a new reset link after the threshold is met.
     *
     * CONFIGURABLE: Can be set per token or globally configured.
     */
    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts; // Initialized in @PrePersist for consistency

    // ═══════════════════════════════════════════════════════════════════════
    // USER INFORMATION
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * User ID (UUID) who requested the password reset.
     * References `users.user_id` in the user-service.
     * NOTE: No foreign key constraint due to microservices architecture.
     */
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    /**
     * User email for confirmation and audit purposes.
     * Denormalized for performance and security verification during the reset process.
     */
    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    // ═══════════════════════════════════════════════════════════════════════
    // TOKEN LIFECYCLE
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Token expiration timestamp.
     * Default: 15 minutes from creation (configurable).
     *
     * SHORT EXPIRY RATIONALE:
     * - Reduces the attack window if the token is compromised.
     * - Encourages timely password reset by the legitimate user.
     * - Follows OWASP security best practices for password reset tokens.
     * - Minimizes the risk of token interception and subsequent misuse.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Token creation timestamp.
     * Automatically set on first save via `@PrePersist`.
     * Used for audit trail and analytics (e.g., token age).
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // ═══════════════════════════════════════════════════════════════════════
    // USAGE TRACKING
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Flag indicating whether the token has been used.
     * Designed for one-time use only for security. Once used, the token is permanently invalid.
     *
     * SECURITY: Prevents token reuse attacks and ensures single-use integrity.
     */
    @Column(nullable = false)
    private boolean used = false; // Default to false upon creation

    /**
     * Timestamp when the token was successfully used (if `used` is true).
     * Used for audit trail, compliance, and analytics.
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    // ═══════════════════════════════════════════════════════════════════════
    // DEVICE TRACKING (Security Audit)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * IP address of the client that requested the token (for security audit).
     * Stored in IPv4/IPv6 format. Max length: 45 chars (IPv6 max length).
     *
     * USAGE:
     * - Detect suspicious geographic patterns (e.g., reset requested from different country).
     * - Help identify compromised accounts.
     * - Satisfy compliance and audit requirements.
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent string of the client that requested the token.
     * Useful for detecting:
     * - Automated bot attacks.
     * - Suspicious device changes.
     * - Platform-specific issues during the reset process.
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    // ═══════════════════════════════════════════════════════════════════════
    // JPA LIFECYCLE CALLBACKS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Pre-persist lifecycle hook.
     * This method is automatically called by JPA before the entity is first
     * persisted (inserted) into the database.
     * It ensures that `createdAt`, `attempts`, and `maxAttempts` are set to
     * their default values if they are `null`, providing robust initialization.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (attempts == null) {
            attempts = 0; // Default to 0 attempts on creation
        }
        if (maxAttempts == null) {
            maxAttempts = 3; // Default security threshold
        }
    }

    /**
     * Pre-update lifecycle hook.
     * This method is automatically called by JPA before the entity is updated
     * in the database.
     * Can be extended for additional audit logging or other logic if needed.
     */
    @PreUpdate
    protected void onUpdate() {
        // Future: Add update audit logic here if needed, e.g., to track changes.
        // Example: Log details of token updates to a security_audit_log table.
    }

    // ═══════════════════════════════════════════════════════════════════════
    // VALIDATION METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Check if the token is expired.
     *
     * @return `true` if the current time is past the expiration time, `false` otherwise.
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Check if the token is valid for a password reset.
     * This is the primary validation method and should be called before
     * attempting to process a password reset request.
     *
     * A token is valid if:
     * - It has not been used (`used` is false).
     * - It has not expired (`isExpired()` is false).
     * - The maximum number of attempts has not been exceeded (`hasExceededAttempts()` is false).
     *
     * @return `true` if the token can be used for password reset, `false` otherwise.
     */
    public boolean isValid() {
        return !used && !isExpired() && !hasExceededAttempts();
    }

    /**
     * Check if the maximum number of allowed attempts for this token has been exceeded.
     *
     * SECURITY: This method returns `true` if `attempts` is greater than or equal to `maxAttempts`.
     * If true, the token should be rejected, and the user prompted to request a new one.
     *
     * @return `true` if attempts >= maxAttempts, `false` otherwise.
     */
    public boolean hasExceededAttempts() {
        return attempts != null && maxAttempts != null && attempts >= maxAttempts;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // STATE MODIFICATION METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Increment the attempt counter for this token.
     *
     * CRITICAL: Call this method *BEFORE* validating the new password.
     * This ensures that failed password attempts are tracked even if the
     * password validation itself fails.
     *
     * USAGE EXAMPLE:
     * ```java
     * token.incrementAttempts();
     * if (passwordService.validate(newPassword)) { // Assume password validation logic
     *     token.markAsUsed();
     *     passwordService.reset(userId, newPassword);
     * }
     * tokenRepository.save(token); // Always save after modifying token state
     * ```
     */
    public void incrementAttempts() {
        if (attempts == null) {
            attempts = 0; // Initialize if null (though @PrePersist handles this)
        }
        attempts++;
    }

    /**
     * Mark this token as used.
     * Sets the `used` flag to `true` and records the timestamp of use.
     * Once marked as used, the token becomes permanently invalid.
     *
     * USAGE: Call this method after a successful password reset operation.
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Get the number of remaining attempts before the token is locked out.
     *
     * Useful for providing user feedback, e.g., "You have 2 attempts remaining."
     *
     * @return The number of attempts remaining (returns 0 if attempts have been exceeded or if counters are null).
     */
    public int getRemainingAttempts() {
        if (attempts == null || maxAttempts == null) {
            return 0; // Should not happen with @PrePersist, but for null safety
        }
        return Math.max(0, maxAttempts - attempts);
    }

    /**
     * Get the remaining validity duration in minutes.
     *
     * Useful for user feedback, e.g., "Link expires in 10 minutes."
     *
     * @return Minutes until expiration (can be negative if expired), or 0 if `expiresAt` is null.
     */
    public long getRemainingMinutes() {
        if (expiresAt == null) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toMinutes();
    }

    /**
     * Check if the token is about to expire (within 2 minutes).
     *
     * Can be used to show an urgent warning to the user on the reset page.
     *
     * @return `true` if the token expires within 2 minutes, `false` otherwise.
     */
    public boolean isExpiringSoon() {
        return getRemainingMinutes() <= 2;
    }

    /**
     * Get the token's age in minutes since its creation.
     *
     * Useful for analytics and security monitoring.
     *
     * @return Minutes since token creation, or 0 if `createdAt` is null.
     */
    public long getAgeInMinutes() {
        if (createdAt == null) {
            return 0;
        }
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
    }

    /**
     * Get a user-friendly validation error message if the token is invalid.
     * Returns a specific reason why the token cannot be used.
     *
     * USAGE:
     * ```java
     * String error = token.getValidationError();
     * if (error != null) {
     *     throw new InvalidTokenException(error); // Or return error to user interface
     * }
     * ```
     *
     * @return A descriptive error message if the token is invalid, or `null` if the token is valid.
     */
    public String getValidationError() {
        if (used) {
            return "This password reset link has already been used. Please request a new one.";
        }
        if (isExpired()) {
            return "This password reset link has expired. Please request a new one.";
        }
        if (hasExceededAttempts()) {
            return "Maximum reset attempts exceeded. Please request a new reset link.";
        }
        return null; // Token is valid
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BUILDER PATTERN HELPER METHODS (Static Factory Methods)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Create a new password reset token with standard configuration.
     * Generates a new UUID for the token string.
     *
     * USAGE:
     * ```java
     * PasswordResetToken token = PasswordResetToken.createToken(
     *     userId,
     *     "user@example.com",
     *     15  // expires in 15 minutes
     * );
     * ```
     *
     * @param userId User UUID
     * @param userEmail User email
     * @param expirationMinutes Minutes until token expires from now
     * @return A new `PasswordResetToken` instance.
     */
    public static PasswordResetToken createToken(
            UUID userId,
            String userEmail,
            int expirationMinutes
    ) {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID().toString()); // Generate a secure random token
        token.setUserId(userId);
        token.setUserEmail(userEmail);
        token.setCreatedAt(LocalDateTime.now()); // Explicitly set, but @PrePersist also handles this
        token.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
        token.setUsed(false);
        token.setAttempts(0); // Explicitly set, but @PrePersist also handles this
        token.setMaxAttempts(3); // Explicitly set, but @PrePersist also handles this
        return token;
    }

    /**
     * Create a new token with device information for enhanced security auditing.
     *
     * USAGE:
     * ```java
     * PasswordResetToken token = PasswordResetToken.createTokenWithDeviceInfo(
     *     userId,
     *     "user@example.com",
     *     15,
     *     request.getRemoteAddr(),    // Example: Getting IP from HttpServletRequest
     *     request.getHeader("User-Agent") // Example: Getting User-Agent from HttpServletRequest
     * );
     * ```
     *
     * @param userId User UUID
     * @param userEmail User email
     * @param expirationMinutes Minutes until token expires
     * @param ipAddress Client IP address (IPv4 or IPv6)
     * @param userAgent Client user agent string
     * @return A new `PasswordResetToken` instance with device information.
     */
    public static PasswordResetToken createTokenWithDeviceInfo(
            UUID userId,
            String userEmail,
            int expirationMinutes,
            String ipAddress,
            String userAgent
    ) {
        // Reuse the basic token creation and then add device info
        PasswordResetToken token = createToken(userId, userEmail, expirationMinutes);
        token.setIpAddress(ipAddress);
        token.setUserAgent(userAgent);
        return token;
    }

    /**
     * Create a new token with a custom maximum attempts threshold.
     * Useful for implementing different security policies based on user roles or context.
     *
     * USAGE:
     * ```java
     * PasswordResetToken token = PasswordResetToken.createTokenWithMaxAttempts(
     *     userId,
     *     "user@example.com",
     *     15,
     *     1 // Very strict: only 1 attempt allowed
     * );
     * ```
     *
     * @param userId User UUID
     * @param userEmail User email
     * @param expirationMinutes Minutes until token expires
     * @param maxAttempts Custom maximum allowed attempts
     * @return A new `PasswordResetToken` instance with the specified maximum attempts.
     */
    public static PasswordResetToken createTokenWithMaxAttempts(
            UUID userId,
            String userEmail,
            int expirationMinutes,
            int maxAttempts
    ) {
        PasswordResetToken token = createToken(userId, userEmail, expirationMinutes);
        token.setMaxAttempts(maxAttempts);
        return token;
    }

    /**
     * Create a fully configured token with all available options.
     * Provides the most granular control over token creation.
     *
     * @param userId User UUID
     * @param userEmail User email
     * @param expirationMinutes Minutes until token expires
     * @param maxAttempts Maximum allowed attempts
     * @param ipAddress Client IP address
     * @param userAgent Client user agent
     * @return A new `PasswordResetToken` instance with all options set.
     */
    public static PasswordResetToken createFullToken(
            UUID userId,
            String userEmail,
            int expirationMinutes,
            int maxAttempts,
            String ipAddress,
            String userAgent
    ) {
        PasswordResetToken token = createToken(userId, userEmail, expirationMinutes);
        token.setMaxAttempts(maxAttempts);
        token.setIpAddress(ipAddress);
        token.setUserAgent(userAgent);
        return token;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SECURITY ANALYSIS METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Calculate a basic token strength score (0-100).
     * This score provides a heuristic measure of the token's security based on its configuration.
     *
     * Factors considered:
     * - Expiration time (shorter = more secure, higher points).
     * - Max attempts (fewer = more secure, higher points).
     * - Device tracking (enabled = more secure, points for IP and User-Agent).
     *
     * @return A security score between 0 and 100.
     */
    public int getSecurityScore() {
        int score = 0;

        // Expiration time scoring (max 40 points)
        // Shorter expiry means higher security
        long expiryMinutes = java.time.Duration.between(createdAt, expiresAt).toMinutes();
        if (expiryMinutes <= 10) score += 40;
        else if (expiryMinutes <= 15) score += 30;
        else if (expiryMinutes <= 30) score += 20;
        else score += 10;

        // Max attempts scoring (max 30 points)
        // Fewer attempts allowed means higher security
        if (maxAttempts != null) {
            if (maxAttempts <= 1) score += 30;
            else if (maxAttempts <= 3) score += 20;
            else if (maxAttempts <= 5) score += 10;
        }

        // Device tracking scoring (max 30 points)
        // Presence of device info adds to auditability and security
        if (ipAddress != null && !ipAddress.isEmpty()) score += 15;
        if (userAgent != null && !userAgent.isEmpty()) score += 15;

        return score;
    }

    /**
     * Check if the token's configuration meets a predefined set of minimum security standards.
     *
     * Current standards:
     * - Expiration time is 30 minutes or less.
     * - Maximum allowed attempts is 5 or less.
     * - The token string itself appears to be a cryptographically random UUID (basic length check).
     *
     * @return `true` if the token meets the minimum security standards, `false` otherwise.
     */
    public boolean meetsSecurityStandards() {
        // Check expiration time (should be short for reset tokens)
        long expiryMinutes = java.time.Duration.between(createdAt, expiresAt).toMinutes();
        if (expiryMinutes > 30) return false;

        // Check max attempts (should be low to prevent brute force)
        if (maxAttempts == null || maxAttempts > 5) return false;

        // Basic check for token format (should be a UUID string)
        if (token == null || token.length() < 32) return false; // UUIDs are 36 chars with hyphens, 32 without

        return true;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // OBJECT OVERRIDES (for better logging and debugging)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Custom `toString()` method for debugging purposes.
     * It masks sensitive information (like the full email and token string)
     * to prevent accidental logging of credentials.
     *
     * @return A safe string representation of the `PasswordResetToken` object.
     */
    @Override
    public String toString() {
        return "PasswordResetToken{" +
                "id=" + id +
                ", userId=" + userId +
                ", userEmail='" + maskEmail(userEmail) + '\'' +
                ", token='" + maskToken(token) + '\'' +
                ", used=" + used +
                ", expired=" + isExpired() +
                ", attempts=" + attempts + "/" + maxAttempts +
                ", remainingMinutes=" + getRemainingMinutes() +
                ", securityScore=" + getSecurityScore() +
                '}';
    }

    /**
     * Helper method to mask the email address for logging and display, enhancing privacy.
     * Example: `user@example.com` becomes `u***@example.com`.
     *
     * @param email The original email string.
     * @return The masked email string.
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        // Show first char, then mask the rest of the local part, keep domain.
        return parts[0].charAt(0) + "***@" + parts[1];
    }

    /**
     * Helper method to mask the token string for logging and display, enhancing security.
     * Example: `abc123def456...xyz789` becomes `abc***789`.
     *
     * @param token The original token string.
     * @return The masked token string.
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) return "***";
        // Show first 3 and last 3 characters, mask the middle.
        return token.substring(0, 3) + "***" + token.substring(token.length() - 3);
    }
}