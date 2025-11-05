package com.youthconnect.auth_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Refresh Token Entity
 *
 * UPDATED: Changed userId from Long to UUID for consistency
 *
 * Stores refresh tokens for JWT authentication.
 * Tokens are long-lived (7 days) and can be revoked.
 *
 * Table: {@code refresh_tokens}
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * JWT refresh token string
     * Must be unique and indexed
     */
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    /**
     * User ID (UUID) who owns this token
     * References users table in auth schema
     */
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    /**
     * User email for quick lookup
     */
    @Column(name = "user_email", nullable = false)
    private String userEmail;

    /**
     * User role for authorization
     */
    @Column(name = "user_role")
    private String userRole;

    /**
     * Token expiration timestamp
     * Default: 7 days from creation
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Token creation timestamp
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Whether token has been revoked
     * Revoked tokens cannot be used for refresh
     */
    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    /**
     * When token was revoked (if revoked)
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    /**
     * Last time token was used for refresh
     * Updated every time token is used
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * IP address of token creation (for security audit)
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent of client that created token
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Number of times token has been used
     */
    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    /**
     * Pre-persist lifecycle hook
     * Sets creation timestamp
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (usageCount == null) {
            usageCount = 0;
        }
    }
}