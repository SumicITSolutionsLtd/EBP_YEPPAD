package com.youthconnect.auth_service.repository;

import com.youthconnect.auth_service.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Refresh Token Repository
 *
 * UPDATED: Added missing count and delete methods for cleanup
 *
 * Handles database operations for refresh tokens.
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find valid (non-revoked) refresh token by token string
     *
     * @param token Token string
     * @return Optional containing token if found and not revoked
     */
    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    /**
     * Find all active refresh tokens for a user
     *
     * @param userId User UUID
     * @return List of active tokens
     */
    List<RefreshToken> findByUserIdAndRevokedFalse(UUID userId);

    /**
     * Find all tokens for a user (including revoked)
     *
     * @param userId User UUID
     * @return List of all tokens
     */
    List<RefreshToken> findByUserId(UUID userId);

    /**
     * Delete expired tokens
     * Used by cleanup scheduler
     *
     * @param dateTime Cutoff datetime
     * @return Number of deleted records
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < ?1")
    long deleteByExpiresAtBefore(LocalDateTime dateTime);

    /**
     * Delete old revoked tokens
     * Used by cleanup scheduler
     *
     * @param dateTime Cutoff datetime for revoked_at
     * @return Number of deleted records
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.revoked = true AND r.revokedAt < ?1")
    long deleteByRevokedTrueAndRevokedAtBefore(LocalDateTime dateTime);

    /**
     * Count revoked tokens
     *
     * @return Number of revoked tokens
     */
    long countByRevokedTrue();

    /**
     * Count expired tokens
     *
     * @param dateTime Current datetime
     * @return Number of expired tokens
     */
    long countByExpiresAtBefore(LocalDateTime dateTime);

    /**
     * Count tokens created after a date
     *
     * @param dateTime Cutoff datetime
     * @return Number of tokens created after date
     */
    long countByCreatedAtAfter(LocalDateTime dateTime);

    /**
     * Find tokens by user email
     *
     * @param email User email
     * @return List of tokens
     */
    List<RefreshToken> findByUserEmail(String email);

    /**
     * Delete all revoked tokens for a user
     *
     * @param userId User UUID
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.userId = ?1 AND r.revoked = true")
    void deleteByUserIdAndRevokedTrue(UUID userId);

    /**
     * Check if user has any active tokens
     *
     * @param userId User UUID
     * @return True if user has at least one active token
     */
    boolean existsByUserIdAndRevokedFalse(UUID userId);

    /**
     * Find most recently created token for user
     *
     * @param userId User UUID
     * @return Optional containing most recent token
     */
    Optional<RefreshToken> findTopByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Count active tokens for user
     *
     * @param userId User UUID
     * @return Number of active tokens
     */
    long countByUserIdAndRevokedFalse(UUID userId);
}