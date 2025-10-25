package com.youthconnect.mentor_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * ============================================================================
 * ERROR RESPONSE DTO
 * ============================================================================
 *
 * Standard error response structure for API error handling.
 * Provides consistent error format across all endpoints.
 *
 * HTTP STATUS CODES:
 * - 400: Bad Request (validation errors)
 * - 401: Unauthorized (authentication required)
 * - 403: Forbidden (insufficient permissions)
 * - 404: Not Found (resource doesn't exist)
 * - 409: Conflict (duplicate or conflicting resource)
 * - 429: Too Many Requests (rate limit exceeded)
 * - 500: Internal Server Error (unexpected server error)
 * - 503: Service Unavailable (service temporarily down)
 *
 * EXAMPLE RESPONSE:
 * {
 *   "timestamp": "2025-01-21T10:30:00Z",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Session with ID 123 not found",
 *   "path": "/api/mentorship/sessions/123"
 * }
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
public class ErrorResponse {

    /**
     * Timestamp when error occurred (ISO-8601 format)
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * HTTP status code (400, 404, 500, etc.)
     */
    private int status;

    /**
     * HTTP status reason phrase ("Bad Request", "Not Found", etc.)
     */
    private String error;

    /**
     * Detailed error message
     */
    private String message;

    /**
     * Request path where error occurred
     */
    private String path;

    /**
     * Optional field for validation errors
     */
    private java.util.Map<String, String> fieldErrors;

    /**
     * Optional trace ID for debugging
     */
    private String traceId;
}