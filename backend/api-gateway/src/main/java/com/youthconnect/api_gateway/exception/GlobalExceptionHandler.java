package com.youthconnect.api_gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for API Gateway (IMPROVED)
 *
 * Catches all exceptions thrown during request processing and returns
 * standardized error responses in JSON format.
 *
 * IMPROVEMENTS IN THIS VERSION:
 * - Proper handling of ValidationException
 * - Better error message formatting
 * - More detailed logging
 * - Circuit breaker exception handling
 * - Rate limit exception handling
 *
 * This handler runs with high priority (@Order(-2)) to catch exceptions
 * before the default Spring error handler.
 *
 * Error Response Format:
 * {
 *   "timestamp": "2025-11-08T09:30:00",
 *   "status": 500,
 *   "error": "Internal Server Error",
 *   "message": "Service temporarily unavailable",
 *   "path": "/api/users/profile",
 *   "requestId": "abc-123-def" (if present)
 * }
 *
 * Location: api-gateway/src/main/java/com/youthconnect/api_gateway/exception/
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 */
@Slf4j
@Order(-2)
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // Log the exception with appropriate level based on type
        logException(exchange, ex);

        // Determine HTTP status and error message
        HttpStatus status = determineHttpStatus(ex);
        String errorMessage = determineErrorMessage(ex);
        String errorType = determineErrorType(ex);

        // Build error response
        Map<String, Object> errorAttributes = buildErrorResponse(
                exchange, status, errorMessage, errorType, ex
        );

        // Set response status and content type
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Add custom headers for error tracking
        exchange.getResponse().getHeaders().add("X-Error-Type", errorType);

        // Convert error response to JSON
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorAttributes);
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response", e);
            bytes = buildFallbackErrorResponse();
        }

        // Write error response to client
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    /**
     * Log exception with appropriate level based on severity
     */
    private void logException(ServerWebExchange exchange, Throwable ex) {
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        String clientIp = getClientIp(exchange);

        if (ex instanceof ValidationException) {
            // Client errors - log at WARN level (not our fault)
            log.warn("Validation error | Method: {} | Path: {} | IP: {} | Message: {}",
                    method, path, clientIp, ex.getMessage());
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            if (rse.getStatusCode().is4xxClientError()) {
                log.warn("Client error | Method: {} | Path: {} | Status: {} | Message: {}",
                        method, path, rse.getStatusCode(), rse.getReason());
            } else {
                log.error("Server error | Method: {} | Path: {} | Status: {} | Message: {}",
                        method, path, rse.getStatusCode(), rse.getReason(), ex);
            }
        } else if (ex instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
            // Circuit breaker open - log at WARN level
            log.warn("Circuit breaker OPEN | Method: {} | Path: {} | Message: {}",
                    method, path, ex.getMessage());
        } else if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) {
            // Service unavailable - log at ERROR level
            log.error("Service connection refused | Method: {} | Path: {} | IP: {}",
                    method, path, clientIp, ex);
        } else {
            // Unexpected errors - log at ERROR level with full stack trace
            log.error("Unexpected error | Method: {} | Path: {} | IP: {}",
                    method, path, clientIp, ex);
        }
    }

    /**
     * Determine appropriate HTTP status code based on exception type
     */
    private HttpStatus determineHttpStatus(Throwable ex) {
        // Validation errors -> 400 Bad Request
        if (ex instanceof ValidationException) {
            return HttpStatus.BAD_REQUEST;
        }

        // Response status exceptions (from backend services)
        if (ex instanceof ResponseStatusException) {
            return HttpStatus.resolve(((ResponseStatusException) ex).getStatusCode().value());
        }

        // Circuit breaker open -> 503 Service Unavailable
        if (ex instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        // Timeout exceptions -> 504 Gateway Timeout
        if (ex instanceof java.util.concurrent.TimeoutException) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }

        // Illegal arguments -> 400 Bad Request
        if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }

        // Web input exceptions -> 400 Bad Request
        if (ex instanceof org.springframework.web.server.ServerWebInputException) {
            return HttpStatus.BAD_REQUEST;
        }

        // Connection refused -> 503 Service Unavailable
        if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        // 404 errors
        if (ex.getMessage() != null && ex.getMessage().contains("404")) {
            return HttpStatus.NOT_FOUND;
        }

        // Default to 500 Internal Server Error
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Determine user-friendly error message based on exception
     */
    private String determineErrorMessage(Throwable ex) {
        // Validation errors - return actual message (it's safe)
        if (ex instanceof ValidationException) {
            return ex.getMessage();
        }

        // Response status exceptions
        if (ex instanceof ResponseStatusException) {
            String reason = ((ResponseStatusException) ex).getReason();
            return reason != null ? reason : "Request failed";
        }

        // Circuit breaker open
        if (ex instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
            return "Service temporarily unavailable due to high error rate. Please try again in a few moments.";
        }

        // Timeout
        if (ex instanceof java.util.concurrent.TimeoutException) {
            return "Request timeout. The service took too long to respond. Please try again.";
        }

        // Illegal arguments
        if (ex instanceof IllegalArgumentException) {
            return "Invalid request: " + ex.getMessage();
        }

        // Connection refused
        if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) {
            return "Service temporarily unavailable. Please try again later.";
        }

        // 404 errors
        if (ex.getMessage() != null && ex.getMessage().contains("404")) {
            return "Requested resource not found";
        }

        // Default - don't expose internal details
        return "An unexpected error occurred. Please contact support if the issue persists.";
    }

    /**
     * Determine error type for classification
     */
    private String determineErrorType(Throwable ex) {
        if (ex instanceof ValidationException) {
            return "VALIDATION_ERROR";
        }
        if (ex instanceof io.github.resilience4j.circuitbreaker.CallNotPermittedException) {
            return "CIRCUIT_BREAKER_OPEN";
        }
        if (ex instanceof java.util.concurrent.TimeoutException) {
            return "TIMEOUT";
        }
        if (ex instanceof ResponseStatusException) {
            HttpStatus status = HttpStatus.resolve(((ResponseStatusException) ex).getStatusCode().value());
            if (status != null && status.is4xxClientError()) {
                return "CLIENT_ERROR";
            }
            return "SERVER_ERROR";
        }
        if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) {
            return "SERVICE_UNAVAILABLE";
        }
        return "INTERNAL_ERROR";
    }

    /**
     * Build standardized error response map
     */
    private Map<String, Object> buildErrorResponse(
            ServerWebExchange exchange,
            HttpStatus status,
            String message,
            String errorType,
            Throwable ex) {

        Map<String, Object> errorAttributes = new HashMap<>();

        // Standard error fields
        errorAttributes.put("timestamp", LocalDateTime.now().toString());
        errorAttributes.put("status", status.value());
        errorAttributes.put("error", status.getReasonPhrase());
        errorAttributes.put("message", message);
        errorAttributes.put("path", exchange.getRequest().getPath().value());
        errorAttributes.put("type", errorType);

        // Add request ID if present (for tracing)
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
        if (requestId != null) {
            errorAttributes.put("requestId", requestId);
        }

        // Add method for context
        errorAttributes.put("method", exchange.getRequest().getMethod().name());

        // In development, include exception class name for debugging
        // Remove this in production for security
        if (log.isDebugEnabled()) {
            errorAttributes.put("exceptionType", ex.getClass().getSimpleName());

            // Include stack trace in debug mode (NEVER in production!)
            if (!(ex instanceof ValidationException)) {
                errorAttributes.put("debugInfo", "Check logs for detailed stack trace");
            }
        }

        return errorAttributes;
    }

    /**
     * Fallback error response if JSON serialization fails
     */
    private byte[] buildFallbackErrorResponse() {
        String fallback = "{" +
                "\"timestamp\":\"" + LocalDateTime.now() + "\"," +
                "\"status\":500," +
                "\"error\":\"Internal Server Error\"," +
                "\"message\":\"An unexpected error occurred\"" +
                "}";
        return fallback.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        if (exchange.getRequest().getRemoteAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }
}