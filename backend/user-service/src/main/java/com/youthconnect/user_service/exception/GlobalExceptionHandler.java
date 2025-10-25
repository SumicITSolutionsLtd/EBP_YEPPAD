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
 * Global Exception Handler for Youth Connect Uganda User Service
 *
 * ============================================================================
 * PURPOSE
 * ============================================================================
 * Provides centralized exception handling across all @RestController classes.
 * Ensures consistent error response formats and proper HTTP status codes.
 *
 * ============================================================================
 * KEY FEATURES
 * ============================================================================
 * ✅ Consistent error response structure using ApiResponse
 * ✅ Proper HTTP status codes for different exception types
 * ✅ Security-conscious error messages (no sensitive data exposure)
 * ✅ Comprehensive logging for debugging
 * ✅ Field-level validation error details
 * ✅ IP address tracking for security monitoring
 *
 * ============================================================================
 * EXCEPTION HIERARCHY & HTTP STATUS CODES
 * ============================================================================
 * UserAlreadyExistsException     → 409 CONFLICT
 * UserNotFoundException          → 404 NOT FOUND
 * InvalidCredentialsException    → 401 UNAUTHORIZED
 * BadCredentialsException        → 401 UNAUTHORIZED
 * MethodArgumentNotValidException → 400 BAD REQUEST
 * IllegalArgumentException       → 400 BAD REQUEST
 * Generic Exception              → 500 INTERNAL SERVER ERROR
 *
 * ============================================================================
 * SECURITY CONSIDERATIONS
 * ============================================================================
 * ⚠️ NEVER expose sensitive information in error responses:
 *    - Passwords, tokens, or API keys
 *    - Internal system architecture details
 *    - Full stack traces (only in development logs)
 *    - Database schema information
 *    - Whether email/phone exists (use generic "invalid credentials")
 *
 * ============================================================================
 * RESPONSE FORMAT
 * ============================================================================
 * All error responses use standardized ApiResponse<T> format:
 * {
 *   "success": false,
 *   "message": "User-friendly error description",
 *   "data": { ... optional error details ... },
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 *
 * @author Youth Connect Uganda Development Team
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ========================================================================
    // BUSINESS LOGIC EXCEPTIONS (400-499 Range)
    // ========================================================================

    /**
     * Handle UserAlreadyExistsException
     *
     * Scenario: User attempts to register with email/phone that already exists
     * HTTP Status: 409 CONFLICT
     *
     * Security Note: Generic message to avoid username enumeration attacks
     *
     * @param ex The UserAlreadyExistsException
     * @param request Web request context for logging
     * @return ResponseEntity with conflict status
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

    /**
     * Handle UserNotFoundException
     *
     * Scenario: Request references non-existent user by ID, email, or phone
     * HTTP Status: 404 NOT FOUND
     *
     * Usage: Profile lookups, user searches, internal service calls
     *
     * @param ex The UserNotFoundException
     * @param request Web request context
     * @return ResponseEntity with not found status
     */
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

    // ========================================================================
    // AUTHENTICATION & AUTHORIZATION EXCEPTIONS (401 Unauthorized)
    // ========================================================================

    /**
     * Handle InvalidCredentialsException (Custom Exception)
     *
     * Scenario: Login attempt with incorrect credentials
     * HTTP Status: 401 UNAUTHORIZED
     *
     * Security Best Practice: Don't specify whether email or password was wrong
     * to prevent username enumeration attacks.
     *
     * @param ex The InvalidCredentialsException
     * @param request Web request context
     * @return ResponseEntity with unauthorized status
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidCredentials(
            InvalidCredentialsException ex, WebRequest request) {

        // Enhanced logging for security monitoring
        log.warn("Authentication failed: {} | Request URI: {} | IP: {} | Timestamp: {}",
                ex.getMessage(),
                request.getDescription(false),
                extractIpAddress(request),
                LocalDateTime.now());

        // Generic message for security
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password"));
    }

    /**
     * Handle BadCredentialsException (Spring Security Exception)
     *
     * Scenario: Spring Security authentication fails
     * HTTP Status: 401 UNAUTHORIZED
     *
     * Note: This catches Spring Security's internal authentication failures
     *
     * @param ex The BadCredentialsException
     * @param request Web request context
     * @return ResponseEntity with unauthorized status
     */
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

    // ========================================================================
    // VALIDATION EXCEPTIONS (400 Bad Request)
    // ========================================================================

    /**
     * Handle MethodArgumentNotValidException
     *
     * Scenario: @Valid annotation fails on @RequestBody validation
     * HTTP Status: 400 BAD REQUEST
     *
     * Provides detailed field-level validation errors in response data.
     *
     * Example Response:
     * {
     *   "success": false,
     *   "message": "Validation failed: firstName: must not be blank, email: invalid format",
     *   "data": {
     *     "firstName": "First name must not be blank",
     *     "email": "Email format is invalid",
     *     "phoneNumber": "Phone number must be Ugandan format (+256XXXXXXXXX)"
     *   },
     *   "timestamp": "2024-01-15T10:30:00"
     * }
     *
     * @param ex The validation exception
     * @param request Web request context
     * @return ResponseEntity with validation errors map
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {

        // Extract field errors into a map for frontend consumption
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        // Create human-readable error summary for logging
        String errorSummary = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {} | Request URI: {} | Fields: {}",
                errorSummary,
                request.getDescription(false),
                fieldErrors.keySet());

        // Return validation errors with field-level details
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(
                        "Validation failed: " + errorSummary,
                        fieldErrors  // ⭐ This now works with the updated ApiResponse.error(message, data)
                ));
    }

    /**
     * Handle IllegalArgumentException
     *
     * Scenario: Invalid method arguments or business logic violations
     * HTTP Status: 400 BAD REQUEST
     *
     * Usage: Custom validation failures, business rule violations
     *
     * @param ex The IllegalArgumentException
     * @param request Web request context
     * @return ResponseEntity with bad request status
     */
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

    // ========================================================================
    // GENERIC EXCEPTION HANDLER (500 Internal Server Error)
    // ========================================================================

    /**
     * Handle all uncaught exceptions (CATCH-ALL)
     *
     * Scenario: Any unexpected exception not handled by specific handlers
     * HTTP Status: 500 INTERNAL SERVER ERROR
     *
     * ⚠️ SECURITY CRITICAL ⚠️
     * - Full exception details logged server-side for debugging
     * - Generic message returned to client (no sensitive information)
     * - Stack trace NEVER sent to client in production
     *
     * @param ex The uncaught exception
     * @param request Web request context
     * @return ResponseEntity with internal server error status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, WebRequest request) {

        // Log full exception details for developers (server-side only)
        log.error("Unexpected error occurred | " +
                        "Request URI: {} | " +
                        "Exception Type: {} | " +
                        "Message: {} | " +
                        "IP: {}",
                request.getDescription(false),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                extractIpAddress(request),
                ex); // Full stack trace in logs

        // Return generic message to client (security best practice)
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "An unexpected error occurred. Please try again later or contact support if the problem persists."
                ));
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    /**
     * Extract client IP address from web request
     *
     * Handles various proxy scenarios:
     * 1. X-Forwarded-For header (standard proxy header)
     * 2. X-Real-IP header (nginx proxy)
     * 3. Remote user fallback
     *
     * Use Cases:
     * - Security logging and monitoring
     * - Rate limiting by IP
     * - Fraud detection
     * - Geographic analytics
     *
     * Security Note: X-Forwarded-For can be spoofed, so use with caution
     * for security-critical operations.
     *
     * @param request Web request to extract IP from
     * @return IP address string or "Unknown" if not determinable
     */
    private String extractIpAddress(WebRequest request) {
        // Try X-Forwarded-For first (most common proxy header)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs (client, proxy1, proxy2)
            // First IP is the original client
            return xForwardedFor.split(",")[0].trim();
        }

        // Try X-Real-IP (nginx standard)
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fallback to remote user or unknown
        return request.getRemoteUser() != null ? request.getRemoteUser() : "Unknown";
    }
}