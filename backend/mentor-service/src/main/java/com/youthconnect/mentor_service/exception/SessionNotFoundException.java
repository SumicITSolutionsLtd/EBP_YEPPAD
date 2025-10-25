package com.youthconnect.mentor_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ============================================================================
 * SESSION NOT FOUND EXCEPTION
 * ============================================================================
 *
 * Exception thrown when a requested mentorship session cannot be found.
 * Results in HTTP 404 Not Found response.
 *
 * USAGE SCENARIOS:
 * - Session ID doesn't exist in database
 * - Session was deleted
 * - Invalid session ID format
 * - User trying to access session they don't have permission for
 *
 * HTTP RESPONSE:
 * - Status Code: 404 Not Found
 * - Error Message: Custom message with session details
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-22
 * ============================================================================
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SessionNotFoundException extends RuntimeException {

    /**
     * Constructor with session ID
     *
     * @param sessionId The ID of the session that was not found
     */
    public SessionNotFoundException(Long sessionId) {
        super("Mentorship session with ID " + sessionId + " not found");
    }

    /**
     * Constructor with custom message
     *
     * @param message Custom error message
     */
    public SessionNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     *
     * @param message Custom error message
     * @param cause   The underlying cause
     */
    public SessionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Static factory method for unauthorized access scenario
     *
     * @param sessionId The session ID
     * @param userId    The user attempting access
     * @return SessionNotFoundException with appropriate message
     */
    public static SessionNotFoundException unauthorizedAccess(Long sessionId, Long userId) {
        return new SessionNotFoundException(String.format(
                "Session with ID %d not found or you don't have permission to access it (User ID: %d)",
                sessionId, userId
        ));
    }
}