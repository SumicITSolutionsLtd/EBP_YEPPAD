package com.youthconnect.user_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Core User entity for the YouthConnect application.
 * This class is the central point of the user-service. It stores the fundamental
 * authentication and authorization information for every single actor in the system
 * (Youth, NGO, Mentor, etc.) and serves as the anchor for linking to specific profile tables.
 *
 * @author Youth Connect Uganda Development Team
 * @version 2.0.0
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone_number", unique = true, length = 20)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified = false;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Builder pattern for easier object creation
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    /**
     * Builder class for User entity
     */
    public static class UserBuilder {
        private String email;
        private String phoneNumber;
        private String passwordHash;
        private Role role;
        private boolean isActive = true;
        private boolean emailVerified = false;
        private boolean phoneVerified = false;

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public UserBuilder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public UserBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public UserBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public UserBuilder emailVerified(boolean emailVerified) {
            this.emailVerified = emailVerified;
            return this;
        }

        public UserBuilder phoneVerified(boolean phoneVerified) {
            this.phoneVerified = phoneVerified;
            return this;
        }

        public User build() {
            User user = new User();
            user.setEmail(this.email);
            user.setPhoneNumber(this.phoneNumber);
            user.setPasswordHash(this.passwordHash);
            user.setRole(this.role);
            user.setActive(this.isActive);
            user.setEmailVerified(this.emailVerified);
            user.setPhoneVerified(this.phoneVerified);
            return user;
        }
    }
}