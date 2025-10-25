package com.youthconnect.mentor_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ============================================================================
 * NOTIFICATION SERVICE FALLBACK
 * ============================================================================
 *
 * Fallback implementation for NotificationServiceClient.
 * Provides graceful degradation when notification-service is unavailable.
 *
 * STRATEGY:
 * - Log failure to monitoring system
 * - Don't block user operations
 * - Notifications can be retried later via scheduled job
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-21
 * ============================================================================
 */
@Component
@Slf4j
public class NotificationServiceClientFallback implements NotificationServiceClient {

    @Override
    public void sendSessionBookedNotification(Long sessionId, Long mentorId, Long menteeId,
                                              LocalDateTime sessionDateTime, String topic) {
        log.error("Failed to send session booked notification. SessionId: {}, MentorId: {}, MenteeId: {}",
                sessionId, mentorId, menteeId);
        // TODO: Queue for retry in notification-service's retry mechanism
    }

    @Override
    public void sendSessionReminder(Long sessionId, Long userId, String reminderType,
                                    LocalDateTime sessionDateTime) {
        log.error("Failed to send session reminder. SessionId: {}, UserId: {}, Type: {}",
                sessionId, userId, reminderType);
    }

    @Override
    public void sendSessionCancelledNotification(Long sessionId, Long mentorId, Long menteeId,
                                                 String cancelledBy, String reason) {
        log.error("Failed to send session cancelled notification. SessionId: {}, CancelledBy: {}",
                sessionId, cancelledBy);
    }

    @Override
    public void sendSessionCompletedNotification(Long sessionId, Long mentorId, Long menteeId) {
        log.error("Failed to send session completed notification. SessionId: {}", sessionId);
    }

    @Override
    public void sendReviewSubmittedNotification(Long reviewId, Long mentorId, Integer rating) {
        log.error("Failed to send review notification. ReviewId: {}, MentorId: {}", reviewId, mentorId);
    }

    @Override
    public void sendNotification(Map<String, Object> request) {
        log.error("Failed to send generic notification: {}", request);
    }
}