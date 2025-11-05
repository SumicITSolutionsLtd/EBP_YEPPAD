package com.youthconnect.auth_service.repository;

import com.youthconnect.auth_service.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Password Reset Token Operations
 *
 * UPDATED: Added missing count and cleanup methods
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Find unused token by token string
     *
     * @param token Token string
     * @return Optional containing token if found and not used
     */
    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    /**
     * Find token by token string (regardless of used status)
     *
     * @param token Token string
     * @return Optional containing token
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Find all tokens for a user
     *
     * @param userId User UUID
     * @return List of tokens
     */
    List<PasswordResetToken> findByUserId(UUID userId);

    /**
     * Find latest token for user
     *
     * @param userId User UUID
     * @return Optional containing latest token
     */
    Optional<PasswordResetToken> findTopByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find all unused tokens for user
     *
     * @param userId User UUID
     * @return List of unused tokens
     */
    List<PasswordResetToken> findByUserIdAndUsedFalse(UUID userId);

    /**
     * Delete expired unused tokens
     * Used by cleanup scheduler
     *
     * @param dateTime Cutoff datetime
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiresAt < ?1 AND p.used = false")
    void deleteByExpiresAtBeforeAndUsedFalse(LocalDateTime dateTime);

    /**
     * Delete old used tokens
     * Used by cleanup scheduler
     *
     * @param dateTime Cutoff datetime
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiresAt < ?1 AND p.used = true")
    void deleteByExpiresAtBeforeAndUsedTrue(LocalDateTime dateTime);

    /**
     * Count used tokens
     *
     * @return Number of used tokens
     */
    long countByUsedTrue();

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
    List<PasswordResetToken> findByUserEmail(String email);

    /**
     * Check if user has pending reset request
     *
     * @param userId User UUID
     * @param now Current timestamp
     * @return True if user has valid unused token
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM PasswordResetToken p " +
            "WHERE p.userId = ?1 AND p.used = false AND p.expiresAt > ?2")
    boolean hasValidTokenForUser(UUID userId, LocalDateTime now);

    /**
     * Delete all tokens for user
     *
     * @param userId User UUID
     */
    @Modifying
    void deleteByUserId(UUID userId);

    /**
     * Count valid (unused, not expired) tokens for user
     *
     * @param userId User UUID
     * @param now Current timestamp
     * @return Number of valid tokens
     */
    @Query("SELECT COUNT(p) FROM PasswordResetToken p " +
            "WHERE p.userId = ?1 AND p.used = false AND p.expiresAt > ?2")
    long countValidTokensForUser(UUID userId, LocalDateTime now);
}