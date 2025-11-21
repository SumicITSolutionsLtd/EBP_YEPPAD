package com.youthconnect.auth_service.repository;

import com.youthconnect.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * User Repository - FIXED VERSION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * CRITICAL FIX:
 * - Changed method: findByOauth2ProviderIsNullAndActiveTrue()
 * - Now correctly references `active` field (not `isActive`)
 *
 * SPRING DATA JPA QUERY DERIVATION:
 * - Method name: findBy[Property][Condition]
 * - Property must match entity field name EXACTLY
 * - For boolean: findBy[Property]True or findBy[Property]False
 *
 * FIELD NAMING:
 * - Entity field: `active` (Boolean)
 * - Database column: `is_active`
 * - Generated methods: getActive(), setActive(), isActive()
 * - Spring Data JPA property: "active"
 *
 * @author Douglas Kings Kato
 * @version 3.0.0 (FIXED)
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // ═══════════════════════════════════════════════════════════════════════
    // TRADITIONAL AUTHENTICATION QUERIES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Find user by email address
     *
     * @param email User's email
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by phone number
     *
     * @param phoneNumber User's phone number (Uganda format)
     * @return Optional containing user if found
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Find user by email OR phone number
     *
     * Used for flexible login (email or phone)
     *
     * @param email Email to search
     * @param phoneNumber Phone number to search
     * @return Optional containing user if found by either
     */
    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);

    /**
     * Check if email already exists
     *
     * Used during registration validation
     *
     * @param email Email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if phone number already exists
     *
     * Used during registration validation
     *
     * @param phoneNumber Phone number to check
     * @return true if phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);

    // ═══════════════════════════════════════════════════════════════════════
    // OAUTH2 AUTHENTICATION QUERIES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Find user by OAuth2 provider and provider's user ID
     *
     * USE CASES:
     * - User logs in with Google → check if Google account exists
     * - User logs in with Facebook → check if Facebook account exists
     *
     * EXAMPLE:
     * ```java
     * Optional<User> user = userRepository.findByOauth2ProviderAndOauth2UserId(
     *     "google",
     *     "1234567890"
     * );
     * ```
     *
     * @param provider OAuth2 provider name ('google', 'facebook', 'apple')
     * @param providerId Provider's unique user ID
     * @return Optional containing user if OAuth2 account exists
     */
    Optional<User> findByOauth2ProviderAndOauth2UserId(String provider, String providerId);

    /**
     * Check if OAuth2 account exists (fast check)
     *
     * USE CASE:
     * - Quick validation before creating new OAuth2 user
     *
     * @param provider OAuth2 provider name
     * @param providerId Provider's user ID
     * @return true if OAuth2 account exists
     */
    boolean existsByOauth2ProviderAndOauth2UserId(String provider, String providerId);

    /**
     * Find user by email and OAuth2 provider
     *
     * USE CASE:
     * - Check if user already linked this provider to their email
     * - Prevent duplicate OAuth2 account linking
     *
     * @param email User's email
     * @param provider OAuth2 provider name
     * @return Optional containing user if email is linked to provider
     */
    Optional<User> findByEmailAndOauth2Provider(String email, String provider);

    /**
     * Find all users authenticated via specific OAuth2 provider
     *
     * USE CASES:
     * - Analytics: How many users use Google vs Facebook login
     * - Maintenance: Notify users if provider integration changes
     * - Security: Audit OAuth2 usage patterns
     *
     * @param provider OAuth2 provider name ('google', 'facebook', etc.)
     * @return List of users using this provider
     */
    List<User> findByOauth2Provider(String provider);

    /**
     * ✅ FIXED: Find all users using traditional password authentication
     *
     * CRITICAL FIX:
     * - Changed from: findByOauth2ProviderIsNullAndIsActiveTrue()
     * - Changed to:   findByOauth2ProviderIsNullAndActiveTrue()
     *
     * WHY THE CHANGE?
     * - Entity field is now named: `active` (not `isActive`)
     * - Spring Data JPA looks for property: "active"
     * - Method name must match: findBy[Property]True
     *
     * USE CASES:
     * - Analytics: Track authentication method usage
     * - Marketing: Target password users for OAuth2 migration
     * - Security: Identify accounts that need strong password policies
     *
     * QUERY GENERATED:
     * ```sql
     * SELECT * FROM users
     * WHERE oauth2_provider IS NULL
     *   AND is_active = TRUE
     * ```
     *
     * @return List of active users without OAuth2 (password-based authentication)
     */
    List<User> findByOauth2ProviderIsNullAndActiveTrue();

    /**
     * Find all users without OAuth2 (regardless of active status)
     *
     * USE CASE:
     * - Full audit of password-based accounts
     *
     * @return List of all users without OAuth2
     */
    List<User> findByOauth2ProviderIsNull();

    /**
     * Count users by authentication method
     *
     * USE CASES:
     * - Dashboard statistics
     * - Authentication method analytics
     *
     * EXAMPLES:
     * ```java
     * long googleUsers = userRepository.countByOauth2Provider("google");
     * long passwordUsers = userRepository.countByOauth2Provider(null);
     * ```
     *
     * @param provider OAuth2 provider name (or NULL for password users)
     * @return Number of users using this authentication method
     */
    long countByOauth2Provider(String provider);

    // ═══════════════════════════════════════════════════════════════════════
    // ADDITIONAL USEFUL QUERIES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * ✅ Find all active users
     *
     * @return List of active users
     */
    List<User> findByActiveTrue();

    /**
     * ✅ Find users by role
     *
     * @param role User role (YOUTH, NGO, MENTOR, etc.)
     * @return List of users with this role
     */
    List<User> findByRole(User.UserRole role);

    /**
     * ✅ Find users by status
     *
     * @param status Account status
     * @return List of users with this status
     */
    List<User> findByStatus(User.AccountStatus status);

    /**
     * ✅ Find verified users
     *
     * @return List of email-verified users
     */
    List<User> findByEmailVerifiedTrue();
}