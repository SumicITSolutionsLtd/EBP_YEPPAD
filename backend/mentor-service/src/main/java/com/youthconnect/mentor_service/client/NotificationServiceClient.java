package com.youthconnect.mentor_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ============================================================================
 * NOTIFICATION SERVICE FEIGN CLIENT
 * ============================================================================
 *
 * Feign client for inter-service communication with notification-service.
 * Handles all notification delivery for mentorship-related events.
 *
 * KEY RESPONSIBILITIES:
 * - Session confirmation notifications
 * - Session reminder notifications (24h, 1h, 15min)
 * - Session cancellation notifications
 * - Session completion notifications
 * - Review submission notifications
 *
 * CIRCUIT BREAKER:
 * - Configured with Resilience4j circuit breaker
 * - Falls back gracefully on failure (logs error, doesn't block flow)
 * - Retry mechanism: 3 attempts with exponential backoff
 *
 * SERVICE DISCOVERY:
 * - Uses Eureka for service discovery
 * - Load balanced across notification-service instances
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-21
 * ============================================================================
 */
@FeignClient(
        name = "notification-service",
        path = "/api/notifications",
        fallback = NotificationServiceClientFallback.class
)
public interface NotificationServiceClient {

    /**
     * Send session booking confirmation notification
     * Notifies both mentor and mentee about new session
     *
     * @param sessionId The ID of the booked session
     * @param mentorId The mentor's user ID
     * @param menteeId The mentee's user ID
     * @param sessionDateTime The scheduled date/time
     * @param topic The session topic
     */
    @PostMapping("/session-booked")
    void sendSessionBookedNotification(
            @RequestParam Long sessionId,
            @RequestParam Long mentorId,
            @RequestParam Long menteeId,
            @RequestParam LocalDateTime sessionDateTime,
            @RequestParam String topic
    );

    /**
     * Send session reminder notification
     * Used for 24h, 1h, and 15min reminders
     *
     * @param sessionId The session ID
     * @param userId The user to notify (mentor or mentee)
     * @param reminderType Type of reminder (24_HOURS, 1_HOUR, 15_MINUTES)
     * @param sessionDateTime The session date/time
     */
    @PostMapping("/session-reminder")
    void sendSessionReminder(
            @RequestParam Long sessionId,
            @RequestParam Long userId,
            @RequestParam String reminderType,
            @RequestParam LocalDateTime sessionDateTime
    );

    /**
     * Send session cancellation notification
     * Notifies both parties when session is cancelled
     *
     * @param sessionId The cancelled session ID
     * @param mentorId The mentor's user ID
     * @param menteeId The mentee's user ID
     * @param cancelledBy Who cancelled (mentor or mentee)
     * @param reason Cancellation reason (optional)
     */
    @PostMapping("/session-cancelled")
    void sendSessionCancelledNotification(
            @RequestParam Long sessionId,
            @RequestParam Long mentorId,
            @RequestParam Long menteeId,
            @RequestParam String cancelledBy,
            @RequestParam(required = false) String reason
    );

    /**
     * Send session completion notification
     * Notifies both parties and prompts for review
     *
     * @param sessionId The completed session ID
     * @param mentorId The mentor's user ID
     * @param menteeId The mentee's user ID
     */
    @PostMapping("/session-completed")
    void sendSessionCompletedNotification(
            @RequestParam Long sessionId,
            @RequestParam Long mentorId,
            @RequestParam Long menteeId
    );

    /**
     * Send review submission notification
     * Notifies mentor when mentee submits a review
     *
     * @param reviewId The review ID
     * @param mentorId The mentor being reviewed
     * @param rating The rating given (1-5)
     */
    @PostMapping("/review-submitted")
    void sendReviewSubmittedNotification(
            @RequestParam Long reviewId,
            @RequestParam Long mentorId,
            @RequestParam Integer rating
    );

    /**
     * Send generic notification with custom template
     * Used for flexible notification scenarios
     *
     * @param request Map containing notification details
     */
    @PostMapping("/send")
    void sendNotification(@RequestBody Map<String, Object> request);
}