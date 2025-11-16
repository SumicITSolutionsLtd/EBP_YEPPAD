package com.youthconnect.auth_service.repository;

import com.youthconnect.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Repository - WITH OAUTH2 QUERIES
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * ✅ NEW METHODS:
 * - findByOauth2ProviderAndOauth2UserId(): Find user by OAuth2 credentials
 * - findByEmailAndOauth2Provider(): Check if email already linked to provider
 * - existsByOauth2ProviderAndOauth2UserId(): Check if OAuth2 account exists
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (OAuth2 Support)
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // ═══════════════════════════════════════════════════════════════════════
    // Traditional Authentication Queries
    // ═══════════════════════════════════════════════════════════════════════

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    // ═══════════════════════════════════════════════════════════════════════
    // ✅ NEW: OAuth2 Authentication Queries
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Find user by OAuth2 provider and provider's user ID
     *
     * USE CASES:
     * - User logs in with Google (check if Google account exists)
     * - User logs in with Facebook (check if Facebook account exists)
     *
     * @param provider OAuth2 provider name ('google', 'facebook', 'apple')
     * @param providerId Provider's unique user ID
     * @return User if OAuth2 account exists
     */
    Optional<User> findByOauth2ProviderAndOauth2UserId(String provider, String providerId);

    /**
     * Check if OAuth2 account exists
     *
     * USE CASE:
     * - Quick check before creating new OAuth2 user
     *
     * @param provider OAuth2 provider name
     * @param providerId Provider's user ID
     * @return true if account exists
     */
    boolean existsByOauth2ProviderAndOauth2UserId(String provider, String providerId);

    /**
     * Find user by email and OAuth2 provider
     *
     * USE CASE:
     * - Check if user already linked this provider to their email
     *
     * @param email User's email
     * @param provider OAuth2 provider name
     * @return User if email is linked to provider
     */
    Optional<User> findByEmailAndOauth2Provider(String email, String provider);

    /**
     * Find all users authenticated via specific OAuth2 provider
     *
     * USE CASE:
     * - Analytics: How many users use Google vs Facebook login
     * - Maintenance: Notify users if provider integration changes
     *
     * @param provider OAuth2 provider name
     * @return List of users using this provider
     */
    List<User> findByOauth2Provider(String provider);

    /**
     * Find all users using traditional password authentication
     *
     * USE CASE:
     * - Analytics: Track authentication method usage
     *
     * @return List of users without OAuth2
     */
    List<User> findByOauth2ProviderIsNull();

    /**
     * Count users by authentication method
     *
     * USE CASE:
     * - Dashboard: Show authentication statistics
     *
     * @param provider OAuth2 provider name (or NULL for password users)
     * @return Number of users using this method
     */
    long countByOauth2Provider(String provider);

    /**
     * Find users who haven't linked OAuth2 account
     *
     * USE CASE:
     * - Marketing: Encourage users to link Google account for easier login
     *
     * @return Users without OAuth2
     */
    List<User> findByOauth2ProviderIsNullAndActiveTrue();
}