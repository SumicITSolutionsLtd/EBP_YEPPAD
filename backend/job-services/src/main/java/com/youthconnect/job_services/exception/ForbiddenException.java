package com.youthconnect.job_services.exception;

import java.util.UUID;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * FORBIDDEN EXCEPTION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Thrown when a user attempts to access or modify a resource they don't have
 * permission to access.
 *
 * HTTP Status: 403 FORBIDDEN
 *
 * Common Use Cases:
 * - User tries to access another user's files
 * - User tries to modify job posting they don't own
 * - User tries to perform admin-only action
 * - User tries to delete another user's application
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-29
 */
public class ForbiddenException extends RuntimeException {

    /**
     * Constructor with custom error message
     *
     * @param message Detailed error message explaining why access is forbidden
     */
    public ForbiddenException(String message) {
        super(message);
    }

    /**
     * Constructor with custom message and cause
     *
     * @param message Error message
     * @param cause Root cause exception
     */
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor for action-based forbidden access
     *
     * @param action The action being attempted (e.g., "delete", "update")
     * @param resource The resource being accessed (e.g., "file", "job posting")
     */
    public ForbiddenException(String action, String resource) {
        super(String.format("You are not authorized to %s this %s", action, resource));
    }

    /**
     * Constructor for user-resource-based forbidden access
     *
     * @param userId User attempting the action
     * @param resourceType Type of resource (e.g., "file", "job")
     * @param resourceId Resource identifier
     */
    public ForbiddenException(UUID userId, String resourceType, UUID resourceId) {
        super(String.format(
                "User %s does not have permission to access %s with ID %s",
                userId,
                resourceType,
                resourceId
        ));
    }
}