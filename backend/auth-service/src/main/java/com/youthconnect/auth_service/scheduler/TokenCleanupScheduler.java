package com.youthconnect.auth_service.scheduler;

import com.youthconnect.auth_service.repository.PasswordResetTokenRepository;
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
 * UPDATED: Added password reset token cleanup
 *
 * Scheduled tasks that run periodically to clean up:
 * - Expired refresh tokens
 * - Old revoked tokens
 * - Expired password reset tokens
 *
 * Schedule:
 * - Token cleanup: Daily at 2:00 AM
 * - Password reset cleanup: Daily at 3:00 AM
 * - Statistics logging: Every hour
 *
 * Benefits:
 * - Reduces database size
 * - Improves query performance
 * - Maintains data hygiene
 * - Security compliance
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    /**
     * Clean Up Expired and Revoked Refresh Tokens
     *
     * Runs daily at 2:00 AM (when system usage is typically low).
     * Deletes:
     * - All tokens that have expired
     * - Revoked tokens older than 7 days (kept for audit purposes)
     *
     * Cron Expression: {@code 0 0 2 * * ?}
     * - Second: 0
     * - Minute: 0
     * - Hour: 2 (2 AM)
     * - Day of Month: * (every day)
     * - Month: * (every month)
     * - Day of Week: ? (any day)
     */
    @Scheduled(cron = "${app.token.blacklist-cleanup-cron:0 0 2 * * ?}")
    @Transactional
    public void cleanupExpiredRefreshTokens() {
        log.info("Starting refresh token cleanup job...");

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime sevenDaysAgo = now.minusDays(7);

            // Delete expired tokens
            long expiredCount = refreshTokenRepository.deleteByExpiresAtBefore(now);
            log.info("Deleted {} expired refresh tokens", expiredCount);

            // Delete old revoked tokens (keep recent ones for audit)
            long revokedCount = refreshTokenRepository.deleteByRevokedTrueAndRevokedAtBefore(sevenDaysAgo);
            log.info("Deleted {} old revoked refresh tokens", revokedCount);

            log.info("Refresh token cleanup completed. Total deleted: {}", expiredCount + revokedCount);

        } catch (Exception e) {
            log.error("Error during refresh token cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Clean Up Expired Password Reset Tokens
     *
     * Runs daily at 3:00 AM.
     * Deletes:
     * - Expired unused tokens older than 24 hours
     * - Used tokens older than 7 days (kept for audit)
     *
     * Cron Expression: {@code 0 0 3 * * ?}
     */
    @Scheduled(cron = "${app.password-reset.cleanup-cron:0 0 3 * * ?}")
    @Transactional
    public void cleanupPasswordResetTokens() {
        log.info("Starting password reset token cleanup job...");

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneDayAgo = now.minusDays(1);
            LocalDateTime sevenDaysAgo = now.minusDays(7);

            // Delete expired unused tokens older than 24 hours
            passwordResetTokenRepository.deleteByExpiresAtBeforeAndUsedFalse(oneDayAgo);
            log.info("Deleted expired unused password reset tokens");

            // Delete old used tokens (keep recent ones for audit)
            passwordResetTokenRepository.deleteByExpiresAtBeforeAndUsedTrue(sevenDaysAgo);
            log.info("Deleted old used password reset tokens");

            log.info("Password reset token cleanup completed");

        } catch (Exception e) {
            log.error("Error during password reset token cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Log Active Token Statistics
     *
     * Runs every hour to monitor token usage.
     * Provides insights for capacity planning and security monitoring.
     *
     * Cron Expression: {@code 0 0 * * * ?}
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional(readOnly = true)
    public void logTokenStatistics() {
        try {
            // Refresh token statistics
            long totalRefreshTokens = refreshTokenRepository.count();
            long revokedRefreshTokens = refreshTokenRepository.countByRevokedTrue();
            long activeRefreshTokens = totalRefreshTokens - revokedRefreshTokens;
            long expiredRefreshTokens = refreshTokenRepository.countByExpiresAtBefore(LocalDateTime.now());

            log.info("Refresh Token Statistics - Total: {}, Active: {}, Revoked: {}, Expired: {}",
                    totalRefreshTokens, activeRefreshTokens, revokedRefreshTokens, expiredRefreshTokens);

            // Password reset token statistics
            long totalResetTokens = passwordResetTokenRepository.count();
            long usedResetTokens = passwordResetTokenRepository.countByUsedTrue();
            long expiredResetTokens = passwordResetTokenRepository.countByExpiresAtBefore(LocalDateTime.now());

            log.info("Password Reset Token Statistics - Total: {}, Used: {}, Expired: {}",
                    totalResetTokens, usedResetTokens, expiredResetTokens);

        } catch (Exception e) {
            log.error("Error logging token statistics: {}", e.getMessage());
        }
    }

    /**
     * Weekly Token Usage Report
     *
     * Runs every Sunday at midnight to generate weekly statistics.
     * Useful for monitoring trends and detecting anomalies.
     *
     * Cron Expression: {@code 0 0 0 ? * SUN}
     */
    @Scheduled(cron = "0 0 0 ? * SUN")
    @Transactional(readOnly = true)
    public void generateWeeklyTokenReport() {
        log.info("Generating weekly token usage report...");

        try {
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

            // Refresh tokens created in last week
            long newRefreshTokens = refreshTokenRepository.countByCreatedAtAfter(oneWeekAgo);

            // Password reset tokens created in last week
            long newResetTokens = passwordResetTokenRepository.countByCreatedAtAfter(oneWeekAgo);

            log.info("Weekly Token Report:");
            log.info("  - New refresh tokens: {}", newRefreshTokens);
            log.info("  - New password reset requests: {}", newResetTokens);

        } catch (Exception e) {
            log.error("Error generating weekly token report: {}", e.getMessage());
        }
    }
}