package com.youthconnect.ai.service.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized Error Response DTO
 *
 * @author Douglas Kings Kato
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Success flag (always false for errors)
     */
    @Builder.Default
    private Boolean success = false;

    /**
     * Error code for client-side handling
     */
    private String errorCode;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * HTTP status code
     */
    private Integer status;

    /**
     * Timestamp of error occurrence
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Request path that caused error
     */
    private String path;

    /**
     * Additional error details (optional)
     */
    private Object details;
}