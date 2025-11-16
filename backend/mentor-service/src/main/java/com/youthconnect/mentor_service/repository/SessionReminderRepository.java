package com.youthconnect.mentor_service.repository;

import com.youthconnect.mentor_service.entity.SessionReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ============================================================================
 * SESSION REMINDER REPOSITORY (UUID VERSION)
 * ============================================================================
 *
 * Data access layer for SessionReminder entity operations.

 *
 * @author Douglas Kings Kato
 * @since 2025-11-06
 * ============================================================================
 */
@Repository
public interface SessionReminderRepository extends JpaRepository<SessionReminder, UUID> {

    /**
     * Find all reminders for a specific session
     *
     * @param sessionId The session UUID
     * @return List of session reminders
     */
    List<SessionReminder> findBySessionId(UUID sessionId);

    /**
     * Check if a reminder exists for a specific session and type
     *
     * @param sessionId The session UUID
     * @param reminderType The reminder type enum
     * @return true if reminder exists
     */
    boolean existsBySessionIdAndReminderType(
            UUID sessionId,
            SessionReminder.ReminderType reminderType
    );

    /**
     * Find reminders that need to be sent to mentors
     * Scheduled time has passed but not yet sent
     *
     * @param time Current timestamp
     * @return List of pending mentor reminders
     */
    List<SessionReminder> findByScheduledTimeBeforeAndSentToMentorFalse(LocalDateTime time);

    /**
     * Find reminders that need to be sent to mentees
     * Scheduled time has passed but not yet sent
     *
     * @param time Current timestamp
     * @return List of pending mentee reminders
     */
    List<SessionReminder> findByScheduledTimeBeforeAndSentToMenteeFalse(LocalDateTime time);
}