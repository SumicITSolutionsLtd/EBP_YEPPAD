package com.youthconnect.mentor_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * ============================================================================
 * INVALID SESSION STATUS EXCEPTION (UUID VERSION)
 * ============================================================================
 *
 * Exception thrown when attempting to perform an operation on a session
 * that is in an invalid status for that operation.
 *
 * UPDATED TO USE UUID:
 * - Session ID parameter now uses UUID instead of Long
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
 * @version 2.0.0 (UUID Support)
 * @since 2025-11-06
 * ============================================================================
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidSessionStatusException extends RuntimeException {

    /**
     * Constructor with session UUID and current status
     *
     * @param sessionId     The UUID of the session
     * @param currentStatus The current status that prevents the operation
     * @param attemptedAction The action that was attempted
     */
    public InvalidSessionStatusException(UUID sessionId, String currentStatus, String attemptedAction) {
        super(String.format(
                "Cannot perform action '%s' on session %s. Current status is '%s'",
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