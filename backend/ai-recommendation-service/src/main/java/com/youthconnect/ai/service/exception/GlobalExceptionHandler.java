package com.youthconnect.ai.service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for AI Recommendation Service
 *
 * Catches and handles all exceptions thrown by the service
 * Returns consistent error responses to clients
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle UserNotFoundException
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {

        log.error("User not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(
                        ex.getErrorCode(),
                        ex.getMessage(),
                        HttpStatus.NOT_FOUND.value(),
                        request.getDescription(false)
                ));
    }

    /**
     * Handle OpportunityNotFoundException
     */
    @ExceptionHandler(OpportunityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOpportunityNotFoundException(
            OpportunityNotFoundException ex, WebRequest request) {

        log.error("Opportunity not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(
                        ex.getErrorCode(),
                        ex.getMessage(),
                        HttpStatus.NOT_FOUND.value(),
                        request.getDescription(false)
                ));
    }

    /**
     * Handle ContentNotFoundException
     */
    @ExceptionHandler(ContentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleContentNotFoundException(
            ContentNotFoundException ex, WebRequest request) {

        log.error("Content not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(
                        ex.getErrorCode(),
                        ex.getMessage(),
                        HttpStatus.NOT_FOUND.value(),
                        request.getDescription(false)
                ));
    }

    /**
     * Handle MentorNotFoundException
     */
    @ExceptionHandler(MentorNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleMentorNotFoundException(
            MentorNotFoundException ex, WebRequest request) {

        log.error("Mentor not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildErrorResponse(
                        ex.getErrorCode(),
                        ex.getMessage(),
                        HttpStatus.NOT_FOUND.value(),
                        request.getDescription(false)
                ));
    }

    /**
     * Handle RecommendationGenerationException
     */
    @ExceptionHandler(RecommendationGenerationException.class)
    public ResponseEntity<Map<String, Object>> handleRecommendationGenerationException(
            RecommendationGenerationException ex, WebRequest request) {

        log.error("Recommendation generation failed: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(
                        ex.getErrorCode(),
                        ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        request.getDescription(false)
                ));
    }

    /**
     * Handle ModelNotReadyException
     */
    @ExceptionHandler(ModelNotReadyException.class)
    public ResponseEntity<Map<String, Object>> handleModelNotReadyException(
            ModelNotReadyException ex, WebRequest request) {

        log.error("ML model not ready: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(buildErrorResponse(
                        ex.getErrorCode(),
                        ex.getMessage(),
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        request.getDescription(false)
                ));
    }

    /**
     * Handle InsufficientDataException
     */
    @ExceptionHandler(InsufficientDataException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientDataException(
            InsufficientDataException ex, WebRequest request) {

        log.warn("Insufficient data: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(
                        ex.getErrorCode(),
                        ex.getMessage(),
                        HttpStatus.BAD_REQUEST.value(),
                        request.getDescription(false)
                ));
    }

    /**
     * Handle ExternalServiceException
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Map<String, Object>> handleExternalServiceException(
            ExternalServiceException ex, WebRequest request) {

        log.error("External service error ({}): {}", ex.getServiceName(), ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(buildErrorResponse(
                        ex.getErrorCode(),
                        ex.getMessage(),
                        HttpStatus.BAD_GATEWAY.value(),
                        request.getDescription(false)
                ));
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        log.error("Invalid argument: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildErrorResponse(
                        "INVALID_ARGUMENT",
                        ex.getMessage(),
                        HttpStatus.BAD_REQUEST.value(),
                        request.getDescription(false)
                ));
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {

        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(
                        "INTERNAL_ERROR",
                        "An unexpected error occurred. Please try again later.",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        request.getDescription(false)
                ));
    }

    /**
     * Build standardized error response
     */
    private Map<String, Object> buildErrorResponse(
            String errorCode,
            String message,
            int status,
            String path) {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("status", status);
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", path.replace("uri=", ""));

        return errorResponse;
    }
}