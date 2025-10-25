package com.youthconnect.auth_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Password Reset Token Entity
 *
 * Stores password reset tokens with expiration and usage tracking.
 *
 * Table: {@code password_reset_tokens}
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "password_reset_tokens")
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
     * User ID who requested password reset
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

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
}