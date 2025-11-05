package com.youthconnect.user_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User Info Response DTO
 *
 * Used for internal service-to-service communication.
 * Contains user information needed by auth-service for authentication.
 *
 * SECURITY NOTE: This DTO includes password hash, which should NEVER
 * be returned to external clients. Only used for internal API responses.
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    /**
     * User's unique identifier
     */
    private UUID userId;

    /**
     * User's email address (web login)
     */
    private String email;

    /**
     * User's phone number (USSD login)
     */
    private String phoneNumber;

    /**
     * BCrypt hashed password
     * CRITICAL: Only for internal use by auth-service
     */
    private String passwordHash;

    /**
     * User role (determines dashboard access)
     * Values: YOUTH, NGO, FUNDER, SERVICE_PROVIDER, MENTOR, ADMIN
     */
    private String role;

    /**
     * Account activation status
     */
    private boolean isActive;

    /**
     * Email verification status
     */
    private boolean emailVerified;

    /**
     * Phone verification status
     */
    private boolean phoneVerified;

    /**
     * Last successful login timestamp
     */
    private LocalDateTime lastLogin;

    /**
     * Failed login attempts counter
     */
    private int failedLoginAttempts;

    /**
     * Account lockout expiry timestamp
     */
    private LocalDateTime accountLockedUntil;

    /**
     * Account creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;

    /**
     * Check if account is locked
     *
     * @return true if account is currently locked
     */
    public boolean isAccountLocked() {
        if (accountLockedUntil == null) {
            return false;
        }
        return accountLockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * Check if account is fully verified
     *
     * @return true if both email and phone are verified
     */
    public boolean isFullyVerified() {
        return emailVerified && phoneVerified;
    }

    /**
     * Check if user can login
     *
     * @return true if account is active and not locked
     */
    public boolean canLogin() {
        return isActive && !isAccountLocked();
    }

    /**
     * Override toString to mask sensitive information
     * Never log password hashes
     */
    @Override
    public String toString() {
        return "UserInfoResponse{" +
                "userId=" + userId +
                ", email='" + maskEmail(email) + '\'' +
                ", phoneNumber='" + maskPhone(phoneNumber) + '\'' +
                ", passwordHash='[PROTECTED]'" +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                ", emailVerified=" + emailVerified +
                ", phoneVerified=" + phoneVerified +
                ", failedLoginAttempts=" + failedLoginAttempts +
                ", accountLocked=" + isAccountLocked() +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * Mask email for logging
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 6) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex > 3) {
            return email.substring(0, 3) + "***" + email.substring(atIndex);
        }
        return "***" + email.substring(atIndex);
    }

    /**
     * Mask phone for logging
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }
}