package com.youthconnect.ussd_service.service;

import com.youthconnect.ussd_service.config.MonitoringConfig;
import com.youthconnect.ussd_service.repository.impl.InMemoryUssdSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled service for cleaning up expired USSD sessions
 * and updating metrics gauges.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionCleanupScheduler {

    private final InMemoryUssdSessionRepository sessionRepository;
    private final MonitoringConfig monitoringConfig;

    /**
     * Cleanup expired sessions every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void cleanupExpiredSessions() {
        try {
            log.debug("Starting scheduled session cleanup");

            // Clean up expired sessions (sessions older than 5 minutes)
            int removedCount = sessionRepository.cleanupExpiredSessions(5);

            // Update active sessions count gauge
            int currentActiveCount = sessionRepository.getSessionCount();
            monitoringConfig.setActiveSessionsCount(currentActiveCount);

            if (removedCount > 0) {
                log.info("Cleaned up {} expired sessions. Active sessions: {}",
                        removedCount, currentActiveCount);
            }

        } catch (Exception e) {
            log.error("Error during session cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Update metrics every minute
     */
    @Scheduled(fixedRate = 60000) // 1 minute
    public void updateMetrics() {
        try {
            int activeCount = sessionRepository.getSessionCount();
            monitoringConfig.setActiveSessionsCount(activeCount);

            // For now, we don't have a queue, so set to 0
            monitoringConfig.setQueuedRequestsCount(0);

        } catch (Exception e) {
            log.error("Error updating metrics: {}", e.getMessage(), e);
        }
    }
}