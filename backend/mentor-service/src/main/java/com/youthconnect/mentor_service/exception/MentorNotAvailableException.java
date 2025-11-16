package com.youthconnect.mentor_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * ============================================================================
 * MENTOR NOT AVAILABLE EXCEPTION (UUID VERSION)
 * ============================================================================
 *
 * Exception thrown when attempting to book a session with a mentor
 * who is not available at the requested time.
 *
 * UPDATED TO USE UUID:
 * - All ID parameters now use UUID instead of Long
 *
 * USAGE SCENARIOS:
 * - Mentor has no availability slot for the requested day/time
 * - Mentor already has a conflicting session scheduled
 * - Mentor's status is set to BUSY or ON_LEAVE
 * - Requested time is outside mentor's working hours
 *
 * HTTP RESPONSE:
 * - Status Code: 409 Conflict
 * - Error Message: Details about why mentor is unavailable
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (UUID Support)
 * @since 2025-11-06
 * ============================================================================
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class MentorNotAvailableException extends RuntimeException {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Constructor with mentor UUID and requested time
     *
     * @param mentorId      The mentor's user UUID
     * @param requestedTime The requested session date/time
     */
    public MentorNotAvailableException(UUID mentorId, LocalDateTime requestedTime) {
        super(String.format(
                "Mentor with ID %s is not available on %s. " +
                        "Please check their availability schedule and try a different time.",
                mentorId, requestedTime.format(FORMATTER)
        ));
    }

    /**
     * Constructor with mentor UUID, requested time, and specific reason
     *
     * @param mentorId      The mentor's user UUID
     * @param requestedTime The requested session date/time
     * @param reason        Specific reason for unavailability
     */
    public MentorNotAvailableException(UUID mentorId, LocalDateTime requestedTime, String reason) {
        super(String.format(
                "Mentor with ID %s is not available on %s. Reason: %s",
                mentorId, requestedTime.format(FORMATTER), reason
        ));
    }

    /**
     * Constructor with custom message
     *
     * @param message Custom error message
     */
    public MentorNotAvailableException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     *
     * @param message Custom error message
     * @param cause   The underlying cause
     */
    public MentorNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor for conflicting session scenario
     *
     * @param mentorId        The mentor's user UUID
     * @param requestedTime   The requested session time
     * @param conflictingSessionId The UUID of the conflicting session
     */
    public static MentorNotAvailableException dueToConflict(
            UUID mentorId,
            LocalDateTime requestedTime,
            UUID conflictingSessionId
    ) {
        return new MentorNotAvailableException(String.format(
                "Mentor with ID %s already has a session scheduled at %s (Session ID: %s). " +
                        "Please choose a different time slot.",
                mentorId, requestedTime.format(FORMATTER), conflictingSessionId
        ));
    }

    /**
     * Constructor for mentor status scenario
     *
     * @param mentorId The mentor's user UUID
     * @param status   The mentor's current status (BUSY, ON_LEAVE, etc.)
     */
    public static MentorNotAvailableException dueToStatus(UUID mentorId, String status) {
        return new MentorNotAvailableException(String.format(
                "Mentor with ID %s is currently %s and not accepting new session requests.",
                mentorId, status
        ));
    }
}