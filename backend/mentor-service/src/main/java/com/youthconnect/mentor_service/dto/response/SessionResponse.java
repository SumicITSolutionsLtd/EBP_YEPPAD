package com.youthconnect.mentor_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ============================================================================
 * SESSION RESPONSE DTO
 * ============================================================================
 *
 * Data Transfer Object for mentorship session response data.
 * Contains complete session information including participant details.
 *
 * USAGE:
 * - API response for session queries
 * - Session list display
 * - Session details page
 *
 * INCLUDES:
 * - Session metadata (ID, datetime, duration, topic)
 * - Status information
 * - Participant details (mentor and mentee names)
 * - Notes (if authorized to view)
 * - Timestamps
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-21
 * ============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {

    /**
     * Session unique identifier
     */
    private Long sessionId;

    /**
     * Mentor user ID
     */
    private Long mentorId;

    /**
     * Mentor full name
     */
    private String mentorName;

    /**
     * Mentee user ID
     */
    private Long menteeId;

    /**
     * Mentee full name
     */
    private String menteeName;

    /**
     * Scheduled session date and time
     */
    private LocalDateTime sessionDatetime;

    /**
     * Session duration in minutes
     */
    private Integer durationMinutes;

    /**
     * Session topic or focus area
     */
    private String topic;

    /**
     * Session status (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW)
     */
    private String status;

    /**
     * Mentor's private notes (only visible to mentor)
     */
    private String mentorNotes;

    /**
     * Mentee's private notes (only visible to mentee)
     */
    private String menteeNotes;

    /**
     * Timestamp when session was created
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp of last update
     */
    private LocalDateTime updatedAt;

    /**
     * Whether session can be cancelled
     */
    public boolean canBeCancelled() {
        return "SCHEDULED".equals(status) &&
                sessionDatetime.isAfter(LocalDateTime.now());
    }

    /**
     * Whether session has already occurred
     */
    public boolean isPast() {
        return sessionDatetime.isBefore(LocalDateTime.now());
    }

    /**
     * Calculate session end time
     */
    public LocalDateTime getSessionEndTime() {
        if (sessionDatetime != null && durationMinutes != null) {
            return sessionDatetime.plusMinutes(durationMinutes);
        }
        return null;
    }
}