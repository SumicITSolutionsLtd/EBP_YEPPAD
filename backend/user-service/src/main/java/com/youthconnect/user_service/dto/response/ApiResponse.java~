package com.youthconnect.user_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized API Response Wrapper
 *
 * <p>This DTO provides a consistent response structure across all Youth Connect services.
 * Every REST API endpoint should return an {@code ApiResponse}, regardless of success or failure.</p>
 *
 * <h3>Structure:</h3>
 * <ul>
 *   <li>{@code success} – indicates if the operation was successful</li>
 *   <li>{@code message} – human-readable description of the outcome</li>
 *   <li>{@code data} – payload returned (nullable for errors)</li>
 *   <li>{@code timestamp} – time when the response was created</li>
 *   <li>{@code errorCode} – optional error classification for clients</li>
 *   <li>{@code path} – optional request path for debugging (useful in error handling)</li>
 * </ul>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Success with data
 * return ResponseEntity.ok(ApiResponse.success(userDto, "User found"));
 *
 * // Success with message only
 * return ResponseEntity.ok(ApiResponse.success("Profile updated successfully"));
 *
 * // Error with message
 * return ResponseEntity.status(HttpStatus.BAD_REQUEST)
 *         .body(ApiResponse.error("Invalid request"));
 *
 * // Error with message + errorCode
 * return ResponseEntity.status(HttpStatus.CONFLICT)
 *         .body(ApiResponse.error("User already exists", "USER_ALREADY_EXISTS"));
 * }</pre>
 *
 * <h3>Benefits:</h3>
 * <ul>
 *   <li>Consistent response contract across services</li>
 *   <li>Simplified error handling on the client side</li>
 *   <li>Supports structured logging & debugging (via {@code path} and {@code timestamp})</li>
 * </ul>
 *
 * @param <T> Type of response payload
 * @author
 *  Youth Connect Uganda Development Team
 * @version 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * Whether the operation was successful.
     * <p>{@code true} for successful operations, {@code false} for errors.</p>
     */
    private boolean success;

    /**
     * Human-readable message describing the result.
     * <p>Examples: "User registered successfully", "Email already exists".</p>
     */
    private String message;

    /**
     * Response payload (optional).
     * <p>Contains the requested data when successful, {@code null} in case of errors.</p>
     */
    private T data;

    /**
     * Timestamp when the response was generated.
     * <p>Useful for debugging and monitoring.</p>
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Error code to classify the error type (optional).
     * <p>Examples: "USER_NOT_FOUND", "VALIDATION_ERROR".</p>
     * <p>Null if the response is successful.</p>
     */
    private String errorCode;

    /**
     * Request path where the error occurred (optional).
     * <p>Primarily useful in error responses for debugging API failures.</p>
     */
    private String path;

    // =========================================================================
    // FACTORY METHODS - RECOMMENDED ENTRY POINTS
    // =========================================================================

    /**
     * Create a success response with data and a message.
     *
     * @param data    Response payload
     * @param message Success message
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a success response with data only (uses default success message).
     *
     * @param data Response payload
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation successful");
    }

    /**
     * Create a success response with a message only (no data).
     *
     * @param message Success message
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a generic error response with a message.
     *
     * @param message Error message
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response with a message and error code.
     *
     * @param message   Error message
     * @param errorCode Error classification code
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response with exception details.
     *
     * @param message   Error message
     * @param exception Exception that occurred
     * @return ApiResponse instance
     */
    public static <T> ApiResponse<T> error(String message, Exception exception) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(exception.getClass().getSimpleName())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
