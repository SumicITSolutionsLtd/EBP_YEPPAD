package com.youthconnect.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_phone_number", columnNames = "phone_number")
        },
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_phone", columnList = "phone_number"),
                @Index(name = "idx_users_oauth2", columnList = "oauth2_provider, oauth2_user_id"),
                // ✅ FIXED: Changed "active" to "is_active" (Physical Column Name)
                @Index(name = "idx_users_active", columnList = "is_active"),
                @Index(name = "idx_users_role", columnList = "role"),
                @Index(name = "idx_users_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id", updatable = false, nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "oauth2_provider", length = 50)
    private String oauth2Provider;

    @Column(name = "oauth2_user_id", length = 255)
    private String oauth2UserId;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    @Builder.Default
    private UserRole role = UserRole.YOUTH;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private AccountStatus status = AccountStatus.PENDING_VERIFICATION;

    // This mapping is correct for the field, but the @Index above needs to match the name="" below
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    @Builder.Default
    private Boolean phoneVerified = false;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", columnDefinition = "uuid")
    private UUID createdBy;

    @Column(name = "updated_by", columnDefinition = "uuid")
    private UUID updatedBy;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", columnDefinition = "uuid")
    private UUID deletedBy;

    @PrePersist
    protected void onCreate() {
        if (failedLoginAttempts == null) failedLoginAttempts = 0;
        if (active == null) active = true;
        if (deleted == null) deleted = false;

        if (isOAuth2User() && (emailVerified == null || !emailVerified)) {
            this.emailVerified = true;
            if (this.status == AccountStatus.PENDING_VERIFICATION) {
                this.status = AccountStatus.ACTIVE;
            }
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return this.active != null && this.active;
    }

    public String getFullName() {
        if (firstName != null && !firstName.isBlank() &&
                lastName != null && !lastName.isBlank()) {
            return firstName + " " + lastName;
        } else if (firstName != null && !firstName.isBlank()) {
            return firstName;
        } else if (lastName != null && !lastName.isBlank()) {
            return lastName;
        }
        return email;
    }

    public boolean isOAuth2User() {
        return oauth2Provider != null && oauth2UserId != null;
    }

    public boolean isPasswordUser() {
        return oauth2Provider == null && oauth2UserId == null;
    }

    public void linkOAuth2Account(String provider, String providerId) {
        this.oauth2Provider = provider;
        this.oauth2UserId = providerId;
        this.emailVerified = true;
    }

    public void unlinkOAuth2Account() {
        this.oauth2Provider = null;
        this.oauth2UserId = null;
    }

    public boolean isAccountLocked() {
        return accountLockedUntil != null &&
                accountLockedUntil.isAfter(LocalDateTime.now());
    }

    public void incrementFailedLoginAttempts() {
        if (this.failedLoginAttempts == null) this.failedLoginAttempts = 0;
        this.failedLoginAttempts++;
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }

    public void lockAccount(int lockoutMinutes) {
        this.accountLockedUntil = LocalDateTime.now().plusMinutes(lockoutMinutes);
        this.status = AccountStatus.LOCKED;
    }

    public void updateLastLogin(String ipAddress) {
        this.lastLogin = LocalDateTime.now();
        this.lastLoginIp = ipAddress;
        resetFailedLoginAttempts();
    }

    public void softDelete(UUID deleterId) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deleterId;
        this.active = false;
        this.status = AccountStatus.INACTIVE;
    }

    public enum UserRole {
        YOUTH, NGO, MENTOR, FUNDER, SERVICE_PROVIDER, ADMIN, SUPER_ADMIN, MODERATOR, COMPANY, RECRUITER, GOVERNMENT
    }

    public enum AccountStatus {
        ACTIVE, INACTIVE, SUSPENDED, LOCKED, PENDING_VERIFICATION, BANNED
    }
}