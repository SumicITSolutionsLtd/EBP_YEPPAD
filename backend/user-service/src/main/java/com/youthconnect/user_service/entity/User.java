package com.youthconnect.user_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * USER ENTITY - FIXED FOR POSTGRESQL ENUM COMPATIBILITY
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * CRITICAL FIX APPLIED:
 * - Added @JdbcTypeCode(SqlTypes.VARCHAR) to force PostgreSQL to treat enum as VARCHAR
 * - This resolves the "operator does not exist: user_role = character varying" error
 *
 * ALTERNATIVE APPROACH (if you want native PostgreSQL enum):
 * - Remove @JdbcTypeCode annotation
 * - Use @Column(columnDefinition = "user_role") instead
 * - But this requires custom Hibernate UserType implementation
 *
 * CURRENT APPROACH (simpler, works out of the box):
 * - Store enum as VARCHAR in database
 * - PostgreSQL compares VARCHAR = VARCHAR (no type mismatch)
 *
 * @author Douglas Kings Kato
 * @version 4.0.0 - PostgreSQL Enum Fix
 * @since 2025-11-21
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_phone", columnList = "phone_number"),
        @Index(name = "idx_user_role", columnList = "role"),
        @Index(name = "idx_user_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
public class User {

    /**
     * Primary Key - UUID for distributed system scalability
     *
     * UUID Benefits:
     * - Globally unique (no collision risk across services)
     * - Secure (not sequential, can't be guessed)
     * - Distributed-friendly (generate without DB roundtrip)
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "user_id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    /**
     * Email Address - Unique identifier for login
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Phone Number - For SMS/USSD authentication
     * Format: E.164 international format (e.g., +256700123456)
     */
    @Column(name = "phone_number", unique = true, length = 20)
    private String phoneNumber;

    /**
     * Password Hash - BCrypt hashed password
     * Never store plain text passwords!
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /**
     * User Role - CRITICAL FIX APPLIED HERE
     *
     * BEFORE (caused error):
     * @Enumerated(EnumType.STRING)
     * @Column(nullable = false, length = 50)
     * private Role role;
     *
     * AFTER (fixed):
     * Added @JdbcTypeCode(SqlTypes.VARCHAR) to force VARCHAR type
     * This tells Hibernate to treat the enum as a VARCHAR in SQL queries
     *
     * WHY THIS WORKS:
     * - Without @JdbcTypeCode: Hibernate generates SQL like:
     *   SELECT * FROM users WHERE role = CAST('YOUTH' AS user_role)
     *   PostgreSQL sees: user_role (enum) = character varying (string) → ERROR
     *
     * - With @JdbcTypeCode: Hibernate generates SQL like:
     *   SELECT * FROM users WHERE role = 'YOUTH'
     *   PostgreSQL sees: character varying = character varying → SUCCESS
     *
     * TRADE-OFFS:
     * ✅ Pro: Works immediately, no custom Hibernate UserType needed
     * ✅ Pro: Still validates enum values at Java level
     * ✅ Pro: Compatible with existing Flyway migrations
     * ⚠️ Con: Loses PostgreSQL enum type checking at DB level
     * ⚠️ Con: Slightly less type-safe in raw SQL queries
     *
     * ALTERNATIVE (if you want native PostgreSQL enum):
     * See commented code block at bottom of this file
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)  // ← THIS IS THE CRITICAL FIX
    @Column(nullable = false, length = 50)
    private Role role;

    /**
     * Account Status - Soft delete flag
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * Email Verification Status
     */
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    /**
     * Phone Verification Status - For USSD users
     */
    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified = false;

    /**
     * Last Login Timestamp - For session management
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /**
     * Failed Login Attempts - For brute force protection
     */
    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts = 0;

    /**
     * Account Lock Timestamp - Lock account after max failed attempts
     */
    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    /**
     * Creation Timestamp - Auto-populated on insert
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Update Timestamp - Auto-updated on every save
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════════════════════════════
    // BUILDER PATTERN IMPLEMENTATION
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Create a new UserBuilder instance
     *
     * Usage:
     * User user = User.builder()
     *     .email("john@example.com")
     *     .phoneNumber("+256700123456")
     *     .passwordHash(hashedPassword)
     *     .role(Role.YOUTH)
     *     .build();
     */
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    /**
     * Fluent Builder for User entity
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

        /**
         * Build the User instance with validation
         */
        public User build() {
            // Validation
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email is required");
            }
            if (passwordHash == null || passwordHash.trim().isEmpty()) {
                throw new IllegalArgumentException("Password hash is required");
            }
            if (role == null) {
                throw new IllegalArgumentException("Role is required");
            }

            // Create user
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

    // ═══════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Check if account is currently locked
     */
    public boolean isAccountLocked() {
        return accountLockedUntil != null &&
                LocalDateTime.now().isBefore(accountLockedUntil);
    }

    /**
     * Increment failed login attempts
     */
    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
    }

    /**
     * Reset failed login attempts (after successful login)
     */
    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }

    /**
     * Lock account for specified minutes
     */
    public void lockAccount(int minutes) {
        this.accountLockedUntil = LocalDateTime.now().plusMinutes(minutes);
    }

    /**
     * Update last login timestamp
     */
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
}

/*
 * ═══════════════════════════════════════════════════════════════════════════
 * ALTERNATIVE APPROACH: NATIVE POSTGRESQL ENUM TYPE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * If you want to use native PostgreSQL enum type (user_role), follow these steps:
 *
 * STEP 1: Remove @JdbcTypeCode(SqlTypes.VARCHAR) from role field
 *
 * STEP 2: Create custom Hibernate UserType
 *
 * package com.youthconnect.user_service.config;
 *
 * import org.hibernate.engine.spi.SharedSessionContractImplementor;
 * import org.hibernate.usertype.UserType;
 *
 * import java.io.Serializable;
 * import java.sql.PreparedStatement;
 * import java.sql.ResultSet;
 * import java.sql.SQLException;
 * import java.sql.Types;
 *
 * public class PostgreSQLEnumType implements UserType<Role> {
 *
 *     @Override
 *     public int getSqlType() {
 *         return Types.OTHER; // PostgreSQL custom type
 *     }
 *
 *     @Override
 *     public Class<Role> returnedClass() {
 *         return Role.class;
 *     }
 *
 *     @Override
 *     public Role nullSafeGet(ResultSet rs, int position,
 *                             SharedSessionContractImplementor session,
 *                             Object owner) throws SQLException {
 *         String value = rs.getString(position);
 *         return value == null ? null : Role.valueOf(value.toUpperCase());
 *     }
 *
 *     @Override
 *     public void nullSafeSet(PreparedStatement st, Role value, int index,
 *                             SharedSessionContractImplementor session) throws SQLException {
 *         if (value == null) {
 *             st.setNull(index, Types.OTHER);
 *         } else {
 *             st.setObject(index, value.name(), Types.OTHER);
 *         }
 *     }
 *
 *     // ... implement other required methods
 * }
 *
 * STEP 3: Annotate role field with custom type
 *
 * @Type(PostgreSQLEnumType.class)
 * @Column(name = "role", nullable = false, columnDefinition = "user_role")
 * private Role role;
 *
 * PROS OF NATIVE ENUM:
 * - Database-level type checking
 * - More efficient storage
 * - Better query performance
 *
 * CONS OF NATIVE ENUM:
 * - More complex setup
 * - Harder to add/remove enum values (requires ALTER TYPE)
 * - Less portable to other databases
 *
 * RECOMMENDATION:
 * Stick with current @JdbcTypeCode(SqlTypes.VARCHAR) approach unless you have
 * specific performance requirements that justify the added complexity.
 */