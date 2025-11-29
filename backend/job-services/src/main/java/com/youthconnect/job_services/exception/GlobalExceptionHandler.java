package com.youthconnect.job_services.exception;

import com.youthconnect.job_services.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * GLOBAL EXCEPTION HANDLER
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Centralized exception handling for all controllers.
 * Provides consistent error responses across the service.
 *
 * Handles:
 * - ResourceNotFoundException (404)
 * - ForbiddenException (403)
 * - DuplicateApplicationException (409)
 * - UnauthorizedAccessException (403)
 * - InvalidJobStatusException (400)
 * - JobExpiredException (400)
 * - MaxApplicationsReachedException (400)
 * - IllegalArgumentException (400)
 * - MaxUploadSizeExceededException (413)
 * - IOException (500)
 * - Validation errors (400)
 * - Generic exceptions (500)
 *
 * @author Douglas Kings Kato
 * @version 3.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ═══════════════════════════════════════════════════════════════════════
    // RESOURCE EXCEPTIONS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Handle ResourceNotFoundException (404)
     *
     * Thrown when requested resource (file, job, application) doesn't exist
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        log.error("❌ Resource not found: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // AUTHORIZATION/PERMISSION EXCEPTIONS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Handle ForbiddenException (403)
     *
     * Thrown when user doesn't have permission to access resource
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(
            ForbiddenException ex, WebRequest request) {
        log.error("❌ Forbidden access: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handle UnauthorizedAccessException (403)
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedAccess(
            UnauthorizedAccessException ex, WebRequest request) {
        log.warn("⚠️ Unauthorized access attempt: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BUSINESS LOGIC EXCEPTIONS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Handle DuplicateApplicationException (409)
     *
     * Thrown when user tries to apply to same job twice
     */
    @ExceptionHandler(DuplicateApplicationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateApplication(
            DuplicateApplicationException ex, WebRequest request) {
        log.warn("⚠️ Duplicate application attempt: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.CONFLICT.value()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handle InvalidJobStatusException (400)
     */
    @ExceptionHandler(InvalidJobStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Void>> handleInvalidJobStatus(
            InvalidJobStatusException ex) {
        log.error("❌ Invalid job status: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle JobExpiredException (400)
     */
    @ExceptionHandler(JobExpiredException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Void>> handleJobExpired(
            JobExpiredException ex) {
        log.error("❌ Job expired: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle MaxApplicationsReachedException (400)
     */
    @ExceptionHandler(MaxApplicationsReachedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Void>> handleMaxApplicationsReached(
            MaxApplicationsReachedException ex) {
        log.warn("⚠️ Max applications reached: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FILE UPLOAD EXCEPTIONS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Handle MaxUploadSizeExceededException (413)
     *
     * Thrown when uploaded file exceeds maximum allowed size
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex) {
        log.error("❌ File too large: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                "File size exceeds maximum allowed size (10MB)",
                HttpStatus.PAYLOAD_TOO_LARGE.value()
        );

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }

    /**
     * Handle InvalidFileException (400)
     *
     * Thrown when uploaded file fails validation
     */
    @ExceptionHandler(InvalidFileException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Void>> handleInvalidFile(
            InvalidFileException ex) {
        log.error("❌ Invalid file: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle IOException (500)
     *
     * Thrown when file I/O operation fails
     */
    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<Void>> handleIOException(IOException ex) {
        log.error("❌ I/O error occurred", ex);

        ApiResponse<Void> response = ApiResponse.error(
                "An error occurred while processing the file. Please try again.",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // VALIDATION EXCEPTIONS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Handle MethodArgumentNotValidException (400)
     *
     * Thrown when @Valid annotation validation fails
     * Returns map of field errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        log.error("❌ Validation failed: {}", ex.getMessage());

        // Extract field errors
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .data(errors)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle IllegalArgumentException (400)
     *
     * Thrown when method receives invalid argument
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex) {
        log.error("❌ Invalid argument: {}", ex.getMessage());

        ApiResponse<Void> response = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GENERIC EXCEPTION HANDLER
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Handle all other exceptions (500)
     *
     * Catches any unhandled exceptions to prevent stack traces from leaking
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("❌ Unexpected error occurred", ex);

        ApiResponse<Void> response = ApiResponse.error(
                "An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}