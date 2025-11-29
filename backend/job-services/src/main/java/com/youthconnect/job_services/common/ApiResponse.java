package com.youthconnect.job_services.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * STANDARD API RESPONSE WRAPPER
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Standardized response format for all REST API endpoints.
 * Provides consistent structure for success and error responses.
 *
 * SUCCESS RESPONSE EXAMPLE:
 * {
 *   "success": true,
 *   "message": "Resource retrieved successfully",
 *   "data": { "id": 1, "name": "John" },
 *   "timestamp": "2025-01-29T10:30:00"
 * }
 *
 * VALIDATION ERROR EXAMPLE:
 * {
 *   "success": false,
 *   "message": "Validation failed",
 *   "errorCode": 400,
 *   "data": { "email": "Invalid format", "age": "Must be 18+" },
 *   "timestamp": "2025-01-29T10:30:00"
 * }
 *
 * @author Douglas Kings Kato
 * @version 2.1.0
 * @param <T> Type of response data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates if request was successful
     * true = success (2xx status)
     * false = error (4xx/5xx status)
     */
    private boolean success;

    /**
     * Human-readable message
     */
    private String message;

    /**
     * Response data
     * - In Success: The payload requested
     * - In Error: Optional details (e.g., validation field errors)
     * - Null if not applicable
     */
    private T data;

    /**
     * Error code (usually matches HTTP status)
     * Only present in error responses.
     */
    private Integer errorCode;

    /**
     * Response timestamp
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ═══════════════════════════════════════════════════════════════════════
    // STATIC FACTORY METHODS - SUCCESS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Create success response with data only.
     *
     * @param data The payload data
     * @param <T> Type of data
     * @return Successful ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create success response with message and data.
     *
     * @param message Success message
     * @param data The payload data
     * @param <T> Type of data
     * @return Successful ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create success response with message only (no data).
     *
     * @param message Success message
     * @param <T> Type inferred
     * @return Successful ApiResponse
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // STATIC FACTORY METHODS - GENERIC ERRORS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Create error response with message and error code.
     *
     * @param message Error message
     * @param errorCode HTTP status code
     * @param <T> Type inferred
     * @return Failed ApiResponse
     */
    public static <T> ApiResponse<T> error(String message, Integer errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response with message, error code, and detailed data.
     * Useful for returning validation maps.
     *
     * @param message Error message
     * @param errorCode HTTP status code
     * @param data Error details (e.g., Map of field errors)
     * @param <T> Type of error data
     * @return Failed ApiResponse
     */
    public static <T> ApiResponse<T> error(String message, Integer errorCode, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create generic internal server error (500).
     *
     * @param message Error message
     * @param <T> Type inferred
     * @return Failed ApiResponse
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(message, 500);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // STATIC FACTORY METHODS - SPECIFIC ERRORS (CONVENIENCE)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 400 Bad Request
     */
    public static <T> ApiResponse<T> validationError(String message) {
        return error(message, 400);
    }

    /**
     * 400 Bad Request with Validation Details
     */
    public static <T> ApiResponse<T> validationError(String message, T data) {
        return error(message, 400, data);
    }

    /**
     * 401 Unauthorized
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return error(message, 401);
    }

    /**
     * 403 Forbidden
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return error(message, 403);
    }

    /**
     * 404 Not Found
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return error(message, 404);
    }
}