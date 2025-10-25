package com.youthconnect.mentor_service.service;

import com.youthconnect.mentor_service.client.NotificationServiceClient;
import com.youthconnect.mentor_service.entity.MentorshipSession;
import com.youthconnect.mentor_service.entity.SessionReminder;
import com.youthconnect.mentor_service.repository.MentorshipSessionRepository;
import com.youthconnect.mentor_service.repository.SessionReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================================
 * SESSION REMINDER SERVICE (FINAL MERGED VERSION)
 * ============================================================================
 *
 * Handles automated session reminders for mentors and mentees.
 * Scheduled job runs periodically to send reminders ahead of sessions.
 *
 * REMINDER INTERVALS:
 * - 24 hours before
 * - 1 hour before
 * - 15 minutes before
 *
 * FEATURES:
 * - Scheduled job every 5 minutes
 * - Batch processing (configurable)
 * - Retry logic per session
 * - Detailed error isolation and metrics logging
 * - Transaction management for data integrity
 *
 * @author
 *   Douglas Kings Kato
 * @version
 *   1.2.0 (Refined & Merged)
 * @since
 *   2025-01-22
 * ============================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionReminderService {

    private final MentorshipSessionRepository sessionRepository;
    private final SessionReminderRepository reminderRepository;
    private final NotificationServiceClient notificationClient;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int BATCH_SIZE = 50; // Limit sessions processed per run

    /**
     * Scheduled job to process session reminders.
     * Runs every 5 minutes to detect upcoming sessions and trigger notifications.
     */
    @Scheduled(cron = "0 */5 * * * *") // Every 5 minutes
    public void processReminders() {
        log.info("=== Starting session reminder processing ===");
        long startTime = System.currentTimeMillis();

        int totalProcessed = 0;
        int successCount = 0;
        int failureCount = 0;

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime checkWindow = now.plusHours(25); // 25-hour window for upcoming sessions

            // Fetch sessions scheduled within the next 25 hours
            List<MentorshipSession> upcomingSessions = sessionRepository
                    .findByStatusAndSessionDatetimeBetween(
                            MentorshipSession.SessionStatus.SCHEDULED,
                            now,
                            checkWindow
                    );

            log.info("Found {} upcoming sessions for reminder check", upcomingSessions.size());

            for (MentorshipSession session : upcomingSessions) {
                totalProcessed++;
                try {
                    boolean success = processSessionReminders(session);
                    if (success) successCount++;
                    else failureCount++;
                } catch (Exception e) {
                    failureCount++;
                    log.error("Error processing reminders for session {}: {}",
                            session.getSessionId(), e.getMessage(), e);
                }

                // Batch limit to prevent overload
                if (totalProcessed >= BATCH_SIZE) {
                    log.info("Processed batch limit of {} sessions; pausing until next cycle", BATCH_SIZE);
                    break;
                }
            }

        } catch (Exception e) {
            log.error("Critical error during session reminder batch processing", e);
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("=== Session reminder processing complete: {} processed | {} success | {} failed | Duration: {} ms ===",
                totalProcessed, successCount, failureCount, duration);
    }

    /**
     * Processes reminder triggers for a specific mentorship session.
     *
     * @param session The mentorship session
     * @return true if all applicable reminders processed successfully
     */
    @Transactional
    protected boolean processSessionReminders(MentorshipSession session) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sessionTime = session.getSessionDatetime();
        boolean allSuccess = true;

        long hoursUntilSession = Duration.between(now, sessionTime).toHours();
        long minutesUntilSession = Duration.between(now, sessionTime).toMinutes();

        // Reminder triggers
        if (hoursUntilSession <= 24 && hoursUntilSession > 23)
            allSuccess &= sendReminder(session, SessionReminder.ReminderType._24_HOURS);

        if (hoursUntilSession <= 1 && minutesUntilSession > 45)
            allSuccess &= sendReminder(session, SessionReminder.ReminderType._1_HOUR);

        if (minutesUntilSession <= 15 && minutesUntilSession > 10)
            allSuccess &= sendReminder(session, SessionReminder.ReminderType._15_MINUTES);

        return allSuccess;
    }

    /**
     * Sends reminder notifications and handles retries for delivery.
     *
     * @param session The mentorship session
     * @param reminderType The reminder type (24h, 1h, 15min)
     * @return true if reminder sent successfully to both participants
     */
    private boolean sendReminder(MentorshipSession session, SessionReminder.ReminderType reminderType) {
        boolean success = true;

        // Avoid duplicate reminders
        if (reminderRepository.existsBySessionIdAndReminderType(session.getSessionId(), reminderType)) {
            log.debug("Reminder already exists for session {} ({})", session.getSessionId(), reminderType);
            return true;
        }

        LocalDateTime scheduledTime = reminderType.calculateScheduledTime(session.getSessionDatetime());
        SessionReminder reminder = SessionReminder.builder()
                .sessionId(session.getSessionId())
                .reminderType(reminderType)
                .scheduledTime(scheduledTime)
                .sentToMentor(false)
                .sentToMentee(false)
                .build();

        reminder = reminderRepository.save(reminder);

        // Send to mentor with retries
        success &= sendWithRetry(() -> notificationClient.sendSessionReminder(
                session.getSessionId(),
                session.getMentorId(),
                reminderType.name(),
                session.getSessionDatetime()
        ), reminder::markAsDeliveredToMentor, "mentor", session.getSessionId(), reminderType);

        // Send to mentee with retries
        success &= sendWithRetry(() -> notificationClient.sendSessionReminder(
                session.getSessionId(),
                session.getMenteeId(),
                reminderType.name(),
                session.getSessionDatetime()
        ), reminder::markAsDeliveredToMentee, "mentee", session.getSessionId(), reminderType);

        reminderRepository.save(reminder);
        return success;
    }

    /**
     * Utility method to execute a send operation with retry attempts.
     */
    private boolean sendWithRetry(Runnable sendAction, Runnable markSuccess,
                                  String recipient, Long sessionId, SessionReminder.ReminderType type) {
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                sendAction.run();
                markSuccess.run();
                log.info("Successfully sent {} reminder to {} for session {} (attempt {}/{})",
                        type, recipient, sessionId, attempt, MAX_RETRY_ATTEMPTS);
                return true;
            } catch (Exception e) {
                log.warn("Failed to send {} reminder to {} for session {} (attempt {}/{}): {}",
                        type, recipient, sessionId, attempt, MAX_RETRY_ATTEMPTS, e.getMessage());
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    log.error("Giving up sending {} reminder to {} after {} attempts",
                            type, recipient, MAX_RETRY_ATTEMPTS);
                }
            }
        }
        return false;
    }

    /**
     * Creates all reminders for a newly scheduled session.
     *
     * @param sessionId The session ID
     */
    @Transactional
    public void createRemindersForSession(Long sessionId) {
        MentorshipSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        for (SessionReminder.ReminderType type : SessionReminder.ReminderType.values()) {
            LocalDateTime scheduledTime = type.calculateScheduledTime(session.getSessionDatetime());

            if (scheduledTime.isAfter(LocalDateTime.now())) {
                SessionReminder reminder = SessionReminder.builder()
                        .sessionId(sessionId)
                        .reminderType(type)
                        .scheduledTime(scheduledTime)
                        .sentToMentor(false)
                        .sentToMentee(false)
                        .build();

                reminderRepository.save(reminder);
                log.info("Created {} reminder for session {} (scheduled for {})", type, sessionId, scheduledTime);
            }
        }
    }

    /**
     * Deletes all reminders for a cancelled session.
     *
     * @param sessionId The session ID
     */
    @Transactional
    public void deleteRemindersForSession(Long sessionId) {
        List<SessionReminder> reminders = reminderRepository.findBySessionId(sessionId);
        reminderRepository.deleteAll(reminders);
        log.info("Deleted {} reminders for cancelled session {}", reminders.size(), sessionId);
    }
}
