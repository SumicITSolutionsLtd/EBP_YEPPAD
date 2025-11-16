package com.youthconnect.auth_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User Entity - WITH OAUTH2 SUPPORT
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * ✅ NEW FIELDS:
 * - oauth2_provider: 'google', 'facebook', 'apple', or NULL
 * - oauth2_user_id: Provider's unique user ID
 *
 * AUTHENTICATION MODES:
 * 1. Email/Password (Traditional)
 *    - oauth2_provider = NULL
 *    - oauth2_user_id = NULL
 *    - password_hash = BCrypt hash
 *
 * 2. OAuth2 (Google/Facebook/Apple)
 *    - oauth2_provider = 'google'
 *    - oauth2_user_id = Google's user ID
 *    - password_hash = Random (unused)
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (OAuth2 Support)
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_phone", columnList = "phone_number"),
        @Index(name = "idx_users_oauth2", columnList = "oauth2_provider, oauth2_user_id"),
        @Index(name = "idx_users_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "user_id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone_number", unique = true, length = 20)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    // ═══════════════════════════════════════════════════════════════════════
    // ✅ NEW: OAuth2 Integration Fields
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * OAuth2 Provider Name
     * Values: 'google', 'facebook', 'apple', or NULL
     * NULL = User uses email/password authentication
     */
    @Column(name = "oauth2_provider", length = 50)
    private String oauth2Provider;

    /**
     * OAuth2 Provider's User ID
     * Example: Google's sub claim (unique user identifier)
     * Must be set together with oauth2_provider
     */
    @Column(name = "oauth2_user_id", length = 255)
    private String oauth2UserId;

    // ═══════════════════════════════════════════════════════════════════════
    // Account Status & Verification
    // ═══════════════════════════════════════════════════════════════════════

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified = false;

    // ═══════════════════════════════════════════════════════════════════════
    // Security & Audit
    // ═══════════════════════════════════════════════════════════════════════

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    // ═══════════════════════════════════════════════════════════════════════
    // Additional Fields
    // ═══════════════════════════════════════════════════════════════════════

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    // ═══════════════════════════════════════════════════════════════════════
    // Audit Fields
    // ═══════════════════════════════════════════════════════════════════════

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", columnDefinition = "uuid")
    private UUID createdBy;

    @Column(name = "updated_by", columnDefinition = "uuid")
    private UUID updatedBy;

    // ═══════════════════════════════════════════════════════════════════════
    // Soft Delete
    // ═══════════════════════════════════════════════════════════════════════

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ═══════════════════════════════════════════════════════════════════════
    // JPA Lifecycle Callbacks
    // ═══════════════════════════════════════════════════════════════════════

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (failedLoginAttempts == null) {
            failedLoginAttempts = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Business Logic Methods
    // ═══════════════════════════════════════════════════════════════════════

    public boolean isLocked() {
        return accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now());
    }

    public boolean isFullyVerified() {
        return emailVerified && phoneVerified;
    }

    public void incrementFailedAttempts() {
        if (failedLoginAttempts == null) {
            failedLoginAttempts = 0;
        }
        failedLoginAttempts++;
    }

    public void resetFailedAttempts() {
        failedLoginAttempts = 0;
        accountLockedUntil = null;
    }

    public void lockAccount(int lockDurationMinutes) {
        accountLockedUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
    }

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.active = false;
    }

    public void updateLastLogin(String ipAddress) {
        this.lastLogin = LocalDateTime.now();
        this.lastLoginIp = ipAddress;
        resetFailedAttempts();
    }

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return email;
    }

    /**
     * ✅ NEW: Check if user uses OAuth2 authentication
     */
    public boolean isOAuth2User() {
        return oauth2Provider != null && oauth2UserId != null;
    }

    /**
     * ✅ NEW: Check if user uses traditional email/password
     */
    public boolean isPasswordUser() {
        return oauth2Provider == null && oauth2UserId == null;
    }

    /**
     * ✅ NEW: Link OAuth2 account to existing user
     */
    public void linkOAuth2Account(String provider, String providerId) {
        this.oauth2Provider = provider;
        this.oauth2UserId = providerId;
        this.emailVerified = true; // OAuth2 providers verify email
    }

    /**
     * ✅ NEW: Unlink OAuth2 account (user switches to password auth)
     */
    public void unlinkOAuth2Account() {
        this.oauth2Provider = null;
        this.oauth2UserId = null;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // User Role Enum
    // ═══════════════════════════════════════════════════════════════════════

    public enum UserRole {
        YOUTH,
        NGO,
        MENTOR,
        FUNDER,
        SERVICE_PROVIDER,
        ADMIN,
        SUPER_ADMIN,
        MODERATOR,
        COMPANY,
        RECRUITER,
        GOVERNMENT
    }
}