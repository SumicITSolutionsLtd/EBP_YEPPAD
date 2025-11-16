package com.youthconnect.mentor_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ============================================================================
 * SESSION REMINDER ENTITY (UUID VERSION - FIXED)
 * ============================================================================
 *
 * Represents automated reminder notifications for mentorship sessions.
 * Tracks reminder delivery status for both mentor and mentee.
 *
 * UPDATED TO USE UUID:
 * - Primary key changed from Long to UUID
 * - session_id foreign key now uses UUID
 *
 * KEY FEATURES:
 * - Multiple reminder types (24h, 1h, 15min before session)
 * - Separate tracking for mentor and mentee delivery
 * - Automatic scheduling based on session datetime
 * - Delivery status tracking
 *
 * DATABASE TABLE: session_reminders (PostgreSQL)
 *
 * WORKFLOW:
 * 1. Session created → Reminders automatically scheduled
 * 2. Scheduled time reached → Notification service triggered
 * 3. Delivery status tracked → Sent flags updated
 * 4. Session completed → Reminders archived
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (UUID Support)
 * @since 2025-11-06
 * ============================================================================
 */
@Entity
@Table(name = "session_reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionReminder {

    /**
     * Unique reminder identifier (Primary Key)
     * Uses UUID for distributed system compatibility
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "reminder_id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID reminderId;

    /**
     * Related session ID (Foreign Key to mentorship_sessions)
     * The session this reminder is for
     *
     * UPDATED: Now uses UUID to match MentorshipSession
     * NOT NULL constraint ensures reminder is always linked to session
     * ON DELETE CASCADE: If session deleted, reminders also deleted
     */
    @Column(name = "session_id", nullable = false, columnDefinition = "UUID")
    private UUID sessionId;

    /**
     * Type of reminder based on timing
     * Determines when reminder should be sent relative to session time
     *
     * REMINDER TYPES:
     * - 24_HOURS: Sent 24 hours before session (advance notice)
     * - 1_HOUR: Sent 1 hour before session (immediate reminder)
     * - 15_MINUTES: Sent 15 minutes before session (final reminder)
     *
     * Multiple reminders created per session (one of each type)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false)
    private ReminderType reminderType;

    /**
     * Scheduled datetime for sending this reminder
     * Calculated as: session_datetime - reminder_offset
     *
     * EXAMPLES:
     * - Session at 2025-01-25 10:00
     * - 24_HOURS reminder scheduled at 2025-01-24 10:00
     * - 1_HOUR reminder scheduled at 2025-01-25 09:00
     * - 15_MINUTES reminder scheduled at 2025-01-25 09:45
     *
     * Used by scheduled job to trigger notifications
     */
    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    /**
     * Delivery status flag for mentor notification
     * Tracks whether reminder was sent to mentor
     *
     * DEFAULT FALSE: Not sent initially
     * Set TRUE after successful notification delivery
     */
    @Column(name = "sent_to_mentor")
    @Builder.Default
    private Boolean sentToMentor = false;

    /**
     * Delivery status flag for mentee notification
     * Tracks whether reminder was sent to mentee
     *
     * DEFAULT FALSE: Not sent initially
     * Set TRUE after successful notification delivery
     */
    @Column(name = "sent_to_mentee")
    @Builder.Default
    private Boolean sentToMentee = false;

    /**
     * Timestamp when both reminders were successfully sent
     * NULL if reminders not yet sent or partially sent
     *
     * Used for audit trail and delivery tracking
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * Timestamp when reminder record was created
     * Automatically set on entity creation
     *
     * IMMUTABLE: Never updated after creation
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Reminder type enumeration
     * Defines timing offset from session datetime
     */
    public enum ReminderType {
        /** Reminder sent 24 hours before session */
        _24_HOURS(24 * 60), // 1440 minutes

        /** Reminder sent 1 hour before session */
        _1_HOUR(60), // 60 minutes

        /** Reminder sent 15 minutes before session */
        _15_MINUTES(15); // 15 minutes

        private final int minutesBeforeSession;

        /**
         * Constructor for ReminderType enum
         *
         * @param minutesBeforeSession Minutes before session to send reminder
         */
        ReminderType(int minutesBeforeSession) {
            this.minutesBeforeSession = minutesBeforeSession;
        }

        /**
         * Get the number of minutes before session
         *
         * @return Integer representing minutes
         */
        public int getMinutesBeforeSession() {
            return minutesBeforeSession;
        }

        /**
         * Calculate scheduled time for this reminder type
         *
         * @param sessionDateTime The session's scheduled datetime
         * @return LocalDateTime when reminder should be sent
         */
        public LocalDateTime calculateScheduledTime(LocalDateTime sessionDateTime) {
            return sessionDateTime.minusMinutes(minutesBeforeSession);
        }
    }

    /**
     * JPA lifecycle callback - executed before persisting new entity
     * Sets the created_at timestamp
     *
     * Called automatically by JPA EntityManager
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Business logic: Check if reminder has been fully delivered
     * Both mentor and mentee must have been notified
     *
     * @return true if both sent flags are true
     */
    public boolean isFullyDelivered() {
        return sentToMentor && sentToMentee;
    }

    /**
     * Business logic: Check if reminder is partially delivered
     * Only one of mentor or mentee has been notified
     *
     * @return true if only one sent flag is true
     */
    public boolean isPartiallyDelivered() {
        return (sentToMentor && !sentToMentee) || (!sentToMentor && sentToMentee);
    }

    /**
     * Business logic: Check if reminder is pending
     * Neither mentor nor mentee has been notified
     *
     * @return true if both sent flags are false
     */
    public boolean isPending() {
        return !sentToMentor && !sentToMentee;
    }

    /**
     * Business logic: Check if reminder time has passed
     * Used to determine if reminder should have been sent
     *
     * @return true if scheduled time is in the past
     */
    public boolean isPastDue() {
        return scheduledTime.isBefore(LocalDateTime.now());
    }

    /**
     * Business logic: Mark reminder as fully delivered
     * Sets both sent flags to true and records sent timestamp
     *
     * Should be called after successful notification to both parties
     */
    public void markAsDelivered() {
        this.sentToMentor = true;
        this.sentToMentee = true;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Business logic: Mark reminder as delivered to mentor only
     * Sets mentor sent flag and updates sent_at if this completes delivery
     */
    public void markAsDeliveredToMentor() {
        this.sentToMentor = true;
        if (isFullyDelivered() && sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
    }

    /**
     * Business logic: Mark reminder as delivered to mentee only
     * Sets mentee sent flag and updates sent_at if this completes delivery
     */
    public void markAsDeliveredToMentee() {
        this.sentToMentee = true;
        if (isFullyDelivered() && sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
    }

    /**
     * Business logic: Get human-readable display name
     *
     * @return String like "24 Hours Before", "1 Hour Before", "15 Minutes Before"
     */
    public String getDisplayName() {
        switch (reminderType) {
            case _24_HOURS:
                return "24 Hours Before";
            case _1_HOUR:
                return "1 Hour Before";
            case _15_MINUTES:
                return "15 Minutes Before";
            default:
                return reminderType.name();
        }
    }
}