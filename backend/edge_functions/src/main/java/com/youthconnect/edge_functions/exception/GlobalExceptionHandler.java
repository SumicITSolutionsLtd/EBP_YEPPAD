package com.youthconnect.edge_functions.exception;

import com.youthconnect.edge_functions.dto.response.ErrorResponse;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Global Exception Handler for Edge Functions Service
 *
 * Provides centralized exception handling across all controllers with:
 * - Consistent error response format
 * - Proper HTTP status codes
 * - Detailed logging for debugging
 * - Client-friendly error messages
 *
 * @author Douglas Kings Kato
 * @version 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ============================================
    // AI SERVICE EXCEPTIONS
    // ============================================

    /**
     * Handle AI service-specific exceptions (OpenAI API errors)
     */
    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<ErrorResponse> handleAIServiceException(
            AIServiceException ex,
            WebRequest request
    ) {
        log.error("❌ AI Service Error: {} - Code: {}", ex.getMessage(), ex.getErrorCode(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .status(ex.getHttpStatus())
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .details(Map.of(
                        "errorType", "AI_SERVICE_ERROR",
                        "suggestion", "The AI assistant is temporarily unavailable. You can still use all other platform features."
                ))
                .build();

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(error);
    }

    // ============================================
    // SERVICE COMMUNICATION EXCEPTIONS
    // ============================================

    /**
     * Handle inter-service communication failures
     */
    @ExceptionHandler(ServiceCommunicationException.class)
    public ResponseEntity<ErrorResponse> handleServiceCommunicationException(
            ServiceCommunicationException ex,
            WebRequest request
    ) {
        log.error("❌ Service Communication Failed: {} -> {}",
                ex.getServiceName(), ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .error("SERVICE_UNAVAILABLE")
                .message(String.format("Unable to communicate with %s. Please try again.", ex.getServiceName()))
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .details(Map.of(
                        "serviceName", ex.getServiceName(),
                        "operation", ex.getOperation(),
                        "retryable", ex.isRetryable()
                ))
                .build();

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(error);
    }

    /**
     * Handle Feign client exceptions (HTTP errors from downstream services)
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(
            FeignException ex,
            WebRequest request
    ) {
        log.error("❌ Feign Client Error: Status {} - {}", ex.status(), ex.getMessage());

        String serviceName = extractServiceNameFromUrl(ex.request().url());
        String errorMessage = String.format("Service %s returned an error. Please try again.", serviceName);

        ErrorResponse error = ErrorResponse.builder()
                .error("EXTERNAL_SERVICE_ERROR")
                .message(errorMessage)
                .status(ex.status())
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .details(Map.of(
                        "serviceName", serviceName,
                        "statusCode", ex.status(),
                        "responseBody", ex.contentUTF8()
                ))
                .build();

        return ResponseEntity
                .status(ex.status())
                .body(error);
    }

    // ============================================
    // RESILIENCE4J EXCEPTIONS
    // ============================================

    /**
     * Handle circuit breaker open state (service failing, requests blocked)
     */
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponse> handleCircuitBreakerException(
            CallNotPermittedException ex,
            WebRequest request
    ) {
        log.warn("⚠️ Circuit Breaker OPEN: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error("SERVICE_TEMPORARILY_UNAVAILABLE")
                .message("This service is experiencing issues. Please try again in a few moments.")
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .details(Map.of(
                        "circuitBreaker", ex.getCausingCircuitBreakerName(),
                        "retryAfter", "10 seconds"
                ))
                .build();

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "10")
                .body(error);
    }

    /**
     * Handle rate limiter exceptions (too many requests)
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleRateLimitException(
            RequestNotPermitted ex,
            WebRequest request
    ) {
        log.warn("⚠️ Rate Limit Exceeded: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error("RATE_LIMIT_EXCEEDED")
                .message("Too many requests. Please slow down and try again later.")
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .details(Map.of(
                        "rateLimiter", "default",
                        "retryAfter", "60 seconds"
                ))
                .build();

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .body(error);
    }

    /**
     * Handle timeout exceptions (requests taking too long)
     */
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeoutException(
            TimeoutException ex,
            WebRequest request
    ) {
        log.error("❌ Request Timeout: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .error("REQUEST_TIMEOUT")
                .message("The request took too long to process. Please try again.")
                .status(HttpStatus.GATEWAY_TIMEOUT.value())
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .details(Map.of(
                        "timeout", "30 seconds",
                        "suggestion", "Try simplifying your request or retry later"
                ))
                .build();

        return ResponseEntity
                .status(HttpStatus.GATEWAY_TIMEOUT)
                .body(error);
    }

    // ============================================
    // VALIDATION EXCEPTIONS
    // ============================================

    /**
     * Handle validation errors (@Valid annotation failures)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        log.warn("⚠️ Validation Failed: {} errors", ex.getBindingResult().getErrorCount());

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse error = ErrorResponse.builder()
                .error("VALIDATION_ERROR")
                .message("Request validation failed. Please check your input.")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .details(Map.of("validationErrors", validationErrors))
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    /**
     * Handle illegal argument exceptions (bad method parameters)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request
    ) {
        log.warn("⚠️ Illegal Argument: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error("INVALID_ARGUMENT")
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    /**
     * Handle type mismatch exceptions (wrong parameter types)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            WebRequest request
    ) {
        log.warn("⚠️ Type Mismatch: {} should be {}", ex.getName(), ex.getRequiredType());

        String message = String.format("Parameter '%s' should be of type %s",
                ex.getName(),
                ex.getRequiredType().getSimpleName());

        ErrorResponse error = ErrorResponse.builder()
                .error("TYPE_MISMATCH")
                .message(message)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    // ============================================
    // SECURITY EXCEPTIONS
    // ============================================

    /**
     * Handle security exceptions (authentication/authorization failures)
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(
            SecurityException ex,
            WebRequest request
    ) {
        log.error("❌ Security Exception: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .error("SECURITY_ERROR")
                .message("Access denied. Please check your credentials.")
                .status(HttpStatus.FORBIDDEN.value())
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .build();

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    // ============================================
    // GENERIC EXCEPTIONS (CATCH-ALL)
    // ============================================

    /**
     * Handle all other unhandled exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request
    ) {
        log.error("❌ Unhandled Exception: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Our team has been notified.")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .details(Map.of(
                        "exceptionType", ex.getClass().getSimpleName(),
                        "note", "This error has been logged for investigation"
                ))
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

    // ============================================
    // UTILITY METHODS
    // ============================================

    /**
     * Extract request path from WebRequest
     */
    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    /**
     * Extract service name from Feign request URL
     */
    private String extractServiceNameFromUrl(String url) {
        try {
            // URL format: http://service-name/api/...
            String[] parts = url.split("/");
            if (parts.length >= 3) {
                return parts[2].split(":")[0]; // Remove port if present
            }
        } catch (Exception e) {
            log.debug("Could not extract service name from URL: {}", url);
        }
        return "external service";
    }
}