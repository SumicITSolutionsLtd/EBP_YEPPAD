package com.youthconnect.auth_service.repository;

import com.youthconnect.auth_service.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for Password Reset Token Operations
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
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
     * Find all tokens for a user
     *
     * @param userId User ID
     * @return Optional containing latest token
     */
    Optional<PasswordResetToken> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Delete expired tokens
     * Used by cleanup scheduler
     *
     * @param dateTime Cutoff datetime
     */
    void deleteByExpiresAtBeforeAndUsedTrue(LocalDateTime dateTime);

    /**
     * Delete old unused expired tokens
     *
     * @param dateTime Cutoff datetime
     */
    void deleteByExpiresAtBeforeAndUsedFalse(LocalDateTime dateTime);
}