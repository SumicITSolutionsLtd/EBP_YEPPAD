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
 * Refresh Token Entity
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Stores refresh tokens for JWT authentication with comprehensive tracking.
 * Tokens are long-lived (7 days) and can be revoked for security.
 *
 * SECURITY FEATURES:
 * ✅ Long-lived tokens for seamless user experience
 * ✅ Revocation support for security
 * ✅ Usage tracking and monitoring
 * ✅ Device information logging
 * ✅ Role-based access control
 *
 * DATABASE TABLE: refresh_tokens
 *
 * @author Douglas Kings Kato
 * @version 3.0.0 (Fixed - Added userRole and lastUsedAt)
 * @since 2025-01-15
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_tokens_token", columnList = "token"),
        @Index(name = "idx_refresh_tokens_user", columnList = "user_id"),
        @Index(name = "idx_refresh_tokens_expires", columnList = "expires_at"),
        @Index(name = "idx_refresh_tokens_revoked", columnList = "revoked")
})
@Data // Lombok annotation to generate getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
@AllArgsConstructor // Lombok annotation to generate a constructor with all fields
public class RefreshToken {

    // ═══════════════════════════════════════════════════════════════════════
    // PRIMARY KEY
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Primary Key - UUID
     * Automatically generated using Hibernate UUID generator.
     * The `updatable = false` ensures the ID cannot be changed after creation.
     * `nullable = false` ensures an ID is always present.
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
     * JWT refresh token string.
     * Must be unique and indexed for fast lookup.
     * Stored as-is (already signed and encrypted by JWT library).
     * `length = 500` provides ample space for longer tokens.
     */
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    // ═══════════════════════════════════════════════════════════════════════
    // USER INFORMATION
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * User ID (UUID) who owns this token.
     * References users.user_id in user-service.
     * NOTE: No foreign key constraint due to microservices architecture.
     */
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    /**
     * User email for quick lookup and validation.
     * Denormalized for performance, reducing need for cross-service calls.
     */
    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    /**
     * User role for authorization.
     * Stored to avoid cross-service calls during token refresh.
     *
     * VALUES: YOUTH, NGO, MENTOR, FUNDER, SERVICE_PROVIDER, ADMIN, etc.
     */
    @Column(name = "user_role", nullable = false, length = 50)
    private String userRole;

    // ═══════════════════════════════════════════════════════════════════════
    // TOKEN LIFECYCLE
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Token expiration timestamp.
     * Default: 7 days from creation.
     * After this time, the token cannot be used for refresh.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Token creation timestamp.
     * Automatically set on first save via `@PrePersist`.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Last time this token was used to refresh an access token.
     * Used for monitoring and detecting suspicious activity.
     * Automatically initialized on creation and updated via `updateLastUsed()`.
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    // ═══════════════════════════════════════════════════════════════════════
    // REVOCATION INFORMATION
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Whether the token has been revoked.
     * Revoked tokens cannot be used for refresh.
     * Default to `false` upon creation.
     *
     * REVOCATION TRIGGERS:
     * - User logout
     * - Password change
     * - Security alert
     * - Manual admin action
     */
    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    /**
     * When the token was revoked (if revoked).
     * Used for audit and cleanup.
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    // ═══════════════════════════════════════════════════════════════════════
    // DEVICE TRACKING (Optional but Recommended)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * IP address of token creation (for security audit).
     * Stored in IPv4/IPv6 format.
     * Max length: 45 chars (IPv6 max length).
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent of the client that created the token.
     * Useful for device identification and security monitoring.
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
     * It ensures that `createdAt` and `lastUsedAt` are set to the current
     * timestamp if they are null, providing default values.
     */
    @PrePersist
    protected void onCreate() {
        // Set creation timestamp if not already set
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        // Initialize lastUsedAt to creation time if not already set.
        // This ensures a value is present even if the token hasn't been used yet.
        if (lastUsedAt == null) {
            lastUsedAt = LocalDateTime.now();
        }
    }

    /**
     * Pre-update lifecycle hook.
     * This method is automatically called by JPA before the entity is updated
     * in the database.
     * Can be used for additional audit logging or other logic if needed.
     */
    @PreUpdate
    protected void onUpdate() {
        // Future: Add update audit logic here if needed, e.g., to track changes.
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BUSINESS LOGIC METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Check if the token is expired.
     *
     * @return true if the current time is past the expiration time, false otherwise.
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Check if the token is valid (not revoked and not expired).
     *
     * @return true if the token can be used for refresh, false otherwise.
     */
    public boolean isValid() {
        return !revoked && !isExpired();
    }

    /**
     * Revoke this token.
     * Sets the `revoked` flag to true and records the revocation timestamp.
     *
     * USE CASES:
     * - User logout
     * - Password change
     * - Security incident
     * - Admin action
     */
    public void revoke() {
        this.revoked = true;
        this.revokedAt = LocalDateTime.now();
    }

    /**
     * Update the `lastUsedAt` timestamp to the current time.
     * This method should be called when the token is successfully used for
     * refreshing an access token, providing usage tracking.
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BUILDER PATTERN HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Create a new refresh token with standard expiration (7 days default).
     * This is a static factory method for convenience.
     *
     * @param userId User UUID
     * @param userEmail User email
     * @param userRole User role
     * @param tokenString JWT token string
     * @param expirationDays Days until token expires from now
     * @return A new `RefreshToken` instance.
     */
    public static RefreshToken createToken(
            UUID userId,
            String userEmail,
            String userRole,
            String tokenString,
            int expirationDays
    ) {
        RefreshToken token = new RefreshToken();
        token.setToken(tokenString);
        token.setUserId(userId);
        token.setUserEmail(userEmail);
        token.setUserRole(userRole);
        token.setCreatedAt(LocalDateTime.now()); // Explicitly set, but @PrePersist also handles this
        token.setLastUsedAt(LocalDateTime.now()); // Explicitly set, but @PrePersist also handles this
        token.setExpiresAt(LocalDateTime.now().plusDays(expirationDays));
        token.setRevoked(false);
        return token;
    }

    /**
     * Create a new refresh token including device information.
     * This is a static factory method for convenience.
     *
     * @param userId User UUID
     * @param userEmail User email
     * @param userRole User role
     * @param tokenString JWT token string
     * @param expirationDays Days until token expires from now
     * @param ipAddress Client IP address
     * @param userAgent Client user agent
     * @return A new `RefreshToken` instance with device information.
     */
    public static RefreshToken createTokenWithDeviceInfo(
            UUID userId,
            String userEmail,
            String userRole,
            String tokenString,
            int expirationDays,
            String ipAddress,
            String userAgent
    ) {
        // Reuse the basic token creation and then add device info
        RefreshToken token = createToken(userId, userEmail, userRole, tokenString, expirationDays);
        token.setIpAddress(ipAddress);
        token.setUserAgent(userAgent);
        return token;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Get the remaining validity duration in seconds.
     *
     * @return Seconds until expiration (can be negative if expired), or 0 if `expiresAt` is null.
     */
    public long getRemainingSeconds() {
        if (expiresAt == null) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
    }

    /**
     * Check if the token is about to expire within a specified time threshold.
     *
     * @param minutesThreshold The number of minutes before expiration to consider "soon".
     * @return true if the token expires within the threshold, false otherwise.
     */
    public boolean isExpiringSoon(int minutesThreshold) {
        if (expiresAt == null) {
            return true; // If no expiration is set, technically it's always "expiring soon"
        }
        return expiresAt.isBefore(LocalDateTime.now().plusMinutes(minutesThreshold));
    }

    /**
     * Get the token's age in hours.
     *
     * @return Hours since token creation, or 0 if `createdAt` is null.
     */
    public long getAgeInHours() {
        if (createdAt == null) {
            return 0;
        }
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toHours();
    }

    /**
     * Check if the token has been used recently (within the last hour).
     *
     * @return true if `lastUsedAt` is within the last hour, false otherwise.
     */
    public boolean isRecentlyUsed() {
        if (lastUsedAt == null) {
            return false;
        }
        return lastUsedAt.isAfter(LocalDateTime.now().minusHours(1));
    }
}