package com.youthconnect.auth_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard API Response Wrapper
 *
 * UPDATED: Added error overload for validation errors
 *
 * Used across all endpoints for consistent response format.
 *
 * @param <T> Type of data payload
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates if request was successful
     */
    private boolean success;

    /**
     * Human-readable message
     */
    private String message;

    /**
     * Response data (null on error)
     */
    private T data;

    /**
     * Timestamp of response
     */
    private Long timestamp;

    /**
     * Create success response with data
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, System.currentTimeMillis());
    }

    /**
     * Create error response with message
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, System.currentTimeMillis());
    }

    /**
     * Create error response with data (for validation errors)
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data, System.currentTimeMillis());
    }
}