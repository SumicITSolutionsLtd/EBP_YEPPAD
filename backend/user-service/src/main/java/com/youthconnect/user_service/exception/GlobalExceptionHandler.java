package com.youthconnect.user_service.exception;

import com.youthconnect.user_service.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * GLOBAL EXCEPTION HANDLER - Centralized Error Management
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * FIXED: Swagger compatibility
 * - basePackages limited to com.youthconnect.user_service.controller
 * - Excludes Springdoc OpenAPI controllers from exception handling
 *
 * @author Douglas Kings Kato
 * @version 2.1.0 - SWAGGER FIX
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.youthconnect.user_service.controller")
public class GlobalExceptionHandler {

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * BUSINESS LOGIC EXCEPTIONS (400-499 Range)
     * ═══════════════════════════════════════════════════════════════════════
     */

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserAlreadyExists(
            UserAlreadyExistsException ex, WebRequest request) {

        log.warn("User registration conflict: {} | Request URI: {} | IP: {}",
                ex.getMessage(),
                request.getDescription(false),
                extractIpAddress(request));

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFound(
            UserNotFoundException ex, WebRequest request) {

        log.warn("User lookup failed: {} | Request URI: {} | IP: {}",
                ex.getMessage(),
                request.getDescription(false),
                extractIpAddress(request));

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * AUTHENTICATION & AUTHORIZATION EXCEPTIONS (401 Unauthorized)
     * ═══════════════════════════════════════════════════════════════════════
     */

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidCredentials(
            InvalidCredentialsException ex, WebRequest request) {

        log.warn("Authentication failed: {} | Request URI: {} | IP: {} | Timestamp: {}",
                ex.getMessage(),
                request.getDescription(false),
                extractIpAddress(request),
                LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(
            BadCredentialsException ex, WebRequest request) {

        log.warn("Spring Security authentication failed | Request URI: {} | IP: {} | Timestamp: {}",
                request.getDescription(false),
                extractIpAddress(request),
                LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password"));
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * VALIDATION EXCEPTIONS (400 Bad Request)
     * ═══════════════════════════════════════════════════════════════════════
     */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        String errorSummary = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {} | Request URI: {} | Fields: {}",
                errorSummary,
                request.getDescription(false),
                fieldErrors.keySet());

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(
                        "Validation failed: " + errorSummary,
                        fieldErrors
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {

        log.warn("Illegal argument: {} | Request URI: {}",
                ex.getMessage(),
                request.getDescription(false));

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * GENERIC EXCEPTION HANDLER (500 Internal Server Error)
     * ═══════════════════════════════════════════════════════════════════════
     */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, WebRequest request) {

        log.error("Unexpected error occurred | " +
                        "Request URI: {} | " +
                        "Exception Type: {} | " +
                        "Message: {} | " +
                        "IP: {}",
                request.getDescription(false),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                extractIpAddress(request),
                ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "An unexpected error occurred. Please try again later or contact support if the problem persists."
                ));
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════
     * HELPER METHODS
     * ═══════════════════════════════════════════════════════════════════════
     */

    private String extractIpAddress(WebRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteUser() != null ? request.getRemoteUser() : "Unknown";
    }
}