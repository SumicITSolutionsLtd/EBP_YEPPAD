package com.youthconnect.mentor_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ============================================================================
 * INVALID SESSION STATUS EXCEPTION
 * ============================================================================
 *
 * Exception thrown when attempting to perform an operation on a session
 * that is in an invalid status for that operation.
 *
 * USAGE SCENARIOS:
 * - Trying to complete an already completed session
 * - Attempting to cancel a session that's already cancelled
 * - Marking a session as in-progress when it's already completed
 *
 * HTTP RESPONSE:
 * - Status Code: 400 Bad Request
 * - Error Message: Descriptive message about the invalid status transition
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-22
 * ============================================================================
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSessionStatusException extends RuntimeException {

    /**
     * Constructor with session ID and current status
     *
     * @param sessionId     The ID of the session
     * @param currentStatus The current status that prevents the operation
     * @param attemptedAction The action that was attempted
     */
    public InvalidSessionStatusException(Long sessionId, String currentStatus, String attemptedAction) {
        super(String.format(
                "Cannot perform action '%s' on session %d. Current status is '%s'",
                attemptedAction, sessionId, currentStatus
        ));
    }

    /**
     * Constructor with custom message
     *
     * @param message Custom error message
     */
    public InvalidSessionStatusException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     *
     * @param message Custom error message
     * @param cause   The underlying cause
     */
    public InvalidSessionStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}