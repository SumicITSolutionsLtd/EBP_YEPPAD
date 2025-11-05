package com.youthconnect.auth_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Password Reset Token Entity
 * Stores password reset tokens with expiration and usage tracking.
 * Tokens are single-use and expire after 15 minutes (configurable).
 *
 * Security Features:
 * - Secure random token (UUID format)
 * - Short expiration time
 * - One-time use only
 * - Attempt tracking
 * - IP and user agent logging
 *
 * Table: {@code password_reset_tokens}
 *
 * @author DOUGLAS KINGS KATO
 * @version 2.0.0
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_user_id_used", columnList = "user_id, used, expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Secure random token (UUID format)
     * Sent to user via email
     */
    @Column(nullable = false, unique = true, length = 255)
    private String token;

    /**
     * User ID who requested password reset (UUID)
     */
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    /**
     * User email for confirmation
     */
    @Column(name = "user_email", nullable = false)
    private String userEmail;

    /**
     * Token expiration timestamp
     * Default: 15 minutes from creation
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Token creation timestamp
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Whether token has been used
     * One-time use only
     */
    @Column(nullable = false)
    private boolean used = false;

    /**
     * When token was used
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * IP address of requester (for security audit)
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent of requester
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Number of failed reset attempts with this token
     */
    @Column(nullable = false)
    private int attempts = 0;

    /**
     * Maximum allowed attempts before token is invalidated
     */
    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts = 3;

    /**
     * Pre-persist lifecycle hook
     * Sets creation timestamp
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Check if token is expired
     *
     * @return true if expired
     */
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Check if token has reached max attempts
     *
     * @return true if max attempts reached
     */
    public boolean hasReachedMaxAttempts() {
        return attempts >= maxAttempts;
    }

    /**
     * Check if token is valid (not used, not expired, attempts available)
     *
     * @return true if valid
     */
    public boolean isValid() {
        return !used && !isExpired() && !hasReachedMaxAttempts();
    }

    /**
     * Increment attempt counter
     */
    public void incrementAttempts() {
        this.attempts++;
    }

    /**
     * Mark token as used
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }
}