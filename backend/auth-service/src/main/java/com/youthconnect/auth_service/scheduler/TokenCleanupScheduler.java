package com.youthconnect.auth_service.scheduler;

import com.youthconnect.auth_service.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Token Cleanup Scheduler
 *
 * Scheduled task that runs periodically to clean up:
 * <ul>
 *     <li>Expired refresh tokens from database</li>
 *     <li>Revoked tokens older than 7 days</li>
 * </ul>
 *
 * Schedule: Every day at 2:00 AM
 *
 * Benefits:
 * <ul>
 *     <li>Reduces database size</li>
 *     <li>Improves query performance</li>
 *     <li>Maintains data hygiene</li>
 * </ul>
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Clean Up Expired and Revoked Tokens
     *
     * Runs daily at 2:00 AM (when system usage is typically low).
     * Deletes:
     * <ul>
     *     <li>All tokens that have expired</li>
     *     <li>Revoked tokens older than 7 days (kept for audit purposes)</li>
     * </ul>
     *
     * Cron Expression: {@code 0 0 2 * * ?}
     * - Second: 0
     * - Minute: 0
     * - Hour: 2 (2 AM)
     * - Day of Month: * (every day)
     * - Month: * (every month)
     * - Day of Week: ? (any day)
     */
    @Scheduled(cron = "0 0 2 * * ?") // 2:00 AM daily
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting token cleanup job...");

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime sevenDaysAgo = now.minusDays(7);

            // Delete expired tokens
            refreshTokenRepository.deleteByExpiresAtBefore(now);
            log.info("Deleted expired refresh tokens");

            // Delete old revoked tokens (keep recent ones for audit)
            long deletedCount = refreshTokenRepository.findAll().stream()
                    .filter(token -> token.isRevoked() &&
                            token.getRevokedAt() != null &&
                            token.getRevokedAt().isBefore(sevenDaysAgo))
                    .peek(refreshTokenRepository::delete)
                    .count();

            log.info("Token cleanup completed. Deleted {} old revoked tokens", deletedCount);

        } catch (Exception e) {
            log.error("Error during token cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Log Active Token Statistics
     *
     * Runs every hour to monitor token usage.
     *
     * Cron Expression: {@code 0 0 * * * ?}
     */
    @Scheduled(cron = "0 0 * * * ?") // Every hour
    @Transactional(readOnly = true)
    public void logTokenStatistics() {
        try {
            long totalTokens = refreshTokenRepository.count();
            long revokedTokens = refreshTokenRepository.findAll().stream()
                    .filter(com.youthconnect.auth_service.entity.RefreshToken::isRevoked)
                    .count();
            long activeTokens = totalTokens - revokedTokens;

            log.info("Token Statistics - Total: {}, Active: {}, Revoked: {}",
                    totalTokens, activeTokens, revokedTokens);

        } catch (Exception e) {
            log.error("Error logging token statistics: {}", e.getMessage());
        }
    }
}