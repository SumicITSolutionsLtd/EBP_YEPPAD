package com.youthconnect.content_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Generic API response wrapper
 * Provides consistent response structure across all endpoints
 *
 * FIXED: Properly handles generic types with Builder pattern
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private Boolean success;
    private String message;
    private T data;
    private String error;
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Success response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Success response with message and data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Error response (NO TYPE PARAMETER NEEDED)
     */
    public static ApiResponse<Void> error(String error) {
        return ApiResponse.<Void>builder()
                .success(false)
                .error(error)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Error response with data (for validation errors)
     */
    public static <T> ApiResponse<T> errorWithData(String error, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }
}