package com.youthconnect.mentor_service.service;

import com.youthconnect.mentor_service.client.NotificationServiceClient;
import com.youthconnect.mentor_service.entity.MentorshipSession;
import com.youthconnect.mentor_service.entity.Review;
import com.youthconnect.mentor_service.repository.MentorshipSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ============================================================================
 * NOTIFICATION INTEGRATION SERVICE
 * ============================================================================
 *
 * Handles all notification-related operations for the mentorship service.
 * Provides a clean abstraction layer over the NotificationServiceClient.
 *
 * KEY RESPONSIBILITIES:
 * - Session booking notifications
 * - Session reminders (24h, 1h, 15min)
 * - Session cancellation notifications
 * - Session completion notifications
 * - Review submission notifications
 * - Error handling and fallback strategies
 *
 * ASYNC EXECUTION:
 * - All notification methods are async to prevent blocking
 * - Uses dedicated notificationExecutor thread pool
 * - Failures logged but don't block main operations
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-22
 * ============================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationIntegrationService {

    private final NotificationServiceClient notificationClient;
    private final MentorshipSessionRepository sessionRepository;

    /**
     * Send session booking confirmation to both mentor and mentee
     * Called immediately after successful session booking
     *
     * @param session The newly booked session
     */
    @Async("notificationExecutor")
    public void sendSessionBookedNotification(MentorshipSession session) {
        log.info("Sending session booked notification for session: {}", session.getSessionId());

        try {
            notificationClient.sendSessionBookedNotification(
                    session.getSessionId(),
                    session.getMentorId(),
                    session.getMenteeId(),
                    session.getSessionDatetime(),
                    session.getTopic()
            );

            log.info("Successfully sent session booked notification for session: {}",
                    session.getSessionId());
        } catch (Exception e) {
            log.error("Failed to send session booked notification for session: {}. Error: {}",
                    session.getSessionId(), e.getMessage(), e);
            // Don't throw - notification failure shouldn't block session creation
        }
    }

    /**
     * Send session reminder notification
     * Called by scheduled job at appropriate times before session
     *
     * @param sessionId The session ID
     * @param userId The user to notify (mentor or mentee)
     * @param reminderType Type of reminder (24_HOURS, 1_HOUR, 15_MINUTES)
     */
    @Async("reminderExecutor")
    public void sendSessionReminder(Long sessionId, Long userId, String reminderType) {
        log.debug("Sending {} reminder to user {} for session {}",
                reminderType, userId, sessionId);

        try {
            MentorshipSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Session not found: " + sessionId));

            notificationClient.sendSessionReminder(
                    sessionId,
                    userId,
                    reminderType,
                    session.getSessionDatetime()
            );

            log.info("Successfully sent {} reminder to user {} for session {}",
                    reminderType, userId, sessionId);
        } catch (Exception e) {
            log.error("Failed to send session reminder. SessionId: {}, UserId: {}, Type: {}. Error: {}",
                    sessionId, userId, reminderType, e.getMessage(), e);
        }
    }

    /**
     * Send session cancellation notification to both parties
     * Called when session is cancelled by either mentor or mentee
     *
     * @param session The cancelled session
     * @param cancelledBy Who cancelled ("mentor" or "mentee")
     * @param reason Optional cancellation reason
     */
    @Async("notificationExecutor")
    public void sendSessionCancelledNotification(
            MentorshipSession session,
            String cancelledBy,
            String reason
    ) {
        log.info("Sending session cancelled notification for session: {}, cancelled by: {}",
                session.getSessionId(), cancelledBy);

        try {
            notificationClient.sendSessionCancelledNotification(
                    session.getSessionId(),
                    session.getMentorId(),
                    session.getMenteeId(),
                    cancelledBy,
                    reason
            );

            log.info("Successfully sent session cancelled notification for session: {}",
                    session.getSessionId());
        } catch (Exception e) {
            log.error("Failed to send session cancelled notification for session: {}. Error: {}",
                    session.getSessionId(), e.getMessage(), e);
        }
    }

    /**
     * Send session completion notification
     * Called when session status is updated to COMPLETED
     * Prompts both parties to submit reviews
     *
     * @param session The completed session
     */
    @Async("notificationExecutor")
    public void sendSessionCompletedNotification(MentorshipSession session) {
        log.info("Sending session completed notification for session: {}",
                session.getSessionId());

        try {
            notificationClient.sendSessionCompletedNotification(
                    session.getSessionId(),
                    session.getMentorId(),
                    session.getMenteeId()
            );

            log.info("Successfully sent session completed notification for session: {}",
                    session.getSessionId());
        } catch (Exception e) {
            log.error("Failed to send session completed notification for session: {}. Error: {}",
                    session.getSessionId(), e.getMessage(), e);
        }
    }

    /**
     * Send review submission notification to mentor
     * Called when mentee submits a review for the mentor
     *
     * @param review The submitted review
     */
    @Async("notificationExecutor")
    public void sendReviewSubmittedNotification(Review review) {
        log.info("Sending review submitted notification for review: {}",
                review.getReviewId());

        try {
            notificationClient.sendReviewSubmittedNotification(
                    review.getReviewId(),
                    review.getRevieweeId(),
                    review.getRating()
            );

            log.info("Successfully sent review submitted notification for review: {}",
                    review.getReviewId());
        } catch (Exception e) {
            log.error("Failed to send review submitted notification for review: {}. Error: {}",
                    review.getReviewId(), e.getMessage(), e);
        }
    }

    /**
     * Send custom notification with flexible parameters
     * Used for special cases not covered by standard methods
     *
     * @param templateName Notification template identifier
     * @param recipientId User ID to receive notification
     * @param parameters Dynamic parameters for template
     */
    @Async("notificationExecutor")
    public void sendCustomNotification(
            String templateName,
            Long recipientId,
            Map<String, Object> parameters
    ) {
        log.info("Sending custom notification '{}' to user: {}", templateName, recipientId);

        try {
            Map<String, Object> request = new HashMap<>();
            request.put("template", templateName);
            request.put("recipientId", recipientId);
            request.putAll(parameters);

            notificationClient.sendNotification(request);

            log.info("Successfully sent custom notification '{}' to user: {}",
                    templateName, recipientId);
        } catch (Exception e) {
            log.error("Failed to send custom notification '{}' to user: {}. Error: {}",
                    templateName, recipientId, e.getMessage(), e);
        }
    }

    /**
     * Send batch reminders for multiple users
     * Used when multiple users need the same notification
     *
     * @param sessionId The session ID
     * @param userIds List of user IDs to notify
     * @param reminderType Type of reminder
     */
    public void sendBatchReminders(Long sessionId, Iterable<Long> userIds, String reminderType) {
        log.info("Sending batch {} reminders for session {}", reminderType, sessionId);

        int successCount = 0;
        int failureCount = 0;

        for (Long userId : userIds) {
            try {
                sendSessionReminder(sessionId, userId, reminderType);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.warn("Failed to send reminder to user {} for session {}",
                        userId, sessionId);
            }
        }

        log.info("Batch reminder complete for session {}. Success: {}, Failed: {}",
                sessionId, successCount, failureCount);
    }

    /**
     * Notify mentor of new session request
     * Called when mentee requests a session
     *
     * @param mentorId The mentor's user ID
     * @param sessionId The requested session ID
     * @param menteeName The mentee's name
     * @param topic The session topic
     * @param requestedTime The requested session datetime
     */
    @Async("notificationExecutor")
    public void sendSessionRequestNotification(
            Long mentorId,
            Long sessionId,
            String menteeName,
            String topic,
            LocalDateTime requestedTime
    ) {
        log.info("Sending session request notification to mentor: {}", mentorId);

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("sessionId", sessionId);
            params.put("menteeName", menteeName);
            params.put("topic", topic);
            params.put("requestedTime", requestedTime.toString());

            sendCustomNotification("session_request", mentorId, params);

            log.info("Successfully sent session request notification to mentor: {}", mentorId);
        } catch (Exception e) {
            log.error("Failed to send session request notification to mentor: {}. Error: {}",
                    mentorId, e.getMessage(), e);
        }
    }
}