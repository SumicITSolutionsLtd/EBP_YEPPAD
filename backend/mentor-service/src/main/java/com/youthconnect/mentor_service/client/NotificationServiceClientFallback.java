package com.youthconnect.mentor_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * ============================================================================
 * NOTIFICATION SERVICE FALLBACK (UUID VERSION - FIXED)
 * ============================================================================
 *
 * Fallback implementation for NotificationServiceClient.
 * Provides graceful degradation when notification-service is unavailable.
 *
 * UPDATED TO USE UUID:
 * - All ID parameters now use UUID instead of Long
 * - Compatible with UUID-based notification service
 *
 * STRATEGY:
 * - Log failure to monitoring system
 * - Don't block user operations
 * - Notifications can be retried later via scheduled job
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (UUID Support)
 * @since 2025-11-06
 * ============================================================================
 */
@Component
@Slf4j
public class NotificationServiceClientFallback implements NotificationServiceClient {

    @Override
    public void sendSessionBookedNotification(UUID sessionId, UUID mentorId, UUID menteeId,
                                              LocalDateTime sessionDateTime, String topic) {
        log.error("Failed to send session booked notification. SessionId: {}, MentorId: {}, MenteeId: {}",
                sessionId, mentorId, menteeId);
        // TODO: Queue for retry in notification-service's retry mechanism
    }

    @Override
    public void sendSessionReminder(UUID sessionId, UUID userId, String reminderType,
                                    LocalDateTime sessionDateTime) {
        log.error("Failed to send session reminder. SessionId: {}, UserId: {}, Type: {}",
                sessionId, userId, reminderType);
    }

    @Override
    public void sendSessionCancelledNotification(UUID sessionId, UUID mentorId, UUID menteeId,
                                                 String cancelledBy, String reason) {
        log.error("Failed to send session cancelled notification. SessionId: {}, CancelledBy: {}",
                sessionId, cancelledBy);
    }

    @Override
    public void sendSessionCompletedNotification(UUID sessionId, UUID mentorId, UUID menteeId) {
        log.error("Failed to send session completed notification. SessionId: {}", sessionId);
    }

    @Override
    public void sendReviewSubmittedNotification(UUID reviewId, UUID mentorId, Integer rating) {
        log.error("Failed to send review notification. ReviewId: {}, MentorId: {}", reviewId, mentorId);
    }

    @Override
    public void sendNotification(Map<String, Object> request) {
        log.error("Failed to send generic notification: {}", request);
    }
}