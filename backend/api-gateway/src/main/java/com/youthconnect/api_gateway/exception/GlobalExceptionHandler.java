package com.youthconnect.api_gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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
 * Global Exception Handler for API Gateway
 *
 * Catches all exceptions thrown during request processing and returns
 * standardized error responses in JSON format.
 *
 * This handler runs with high priority (@Order(-2)) to catch exceptions
 * before the default Spring error handler.
 *
 * Error Response Format:
 * {
 *   "timestamp": "2025-01-20T10:30:00",
 *   "status": 500,
 *   "error": "Internal Server Error",
 *   "message": "Service temporarily unavailable",
 *   "path": "/api/users/profile"
 * }
 *
 * Location: api-gateway/src/main/java/com/youthconnect/api_gateway/exception/
 */
@Slf4j
@Order(-2)
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // Log the exception with full stack trace
        log.error("Error processing request: {} {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath(),
                ex);

        // Determine HTTP status and error message
        HttpStatus status = determineHttpStatus(ex);
        String errorMessage = determineErrorMessage(ex);

        // Build error response
        Map<String, Object> errorAttributes = buildErrorResponse(
                exchange, status, errorMessage, ex
        );

        // Set response status and content type
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Convert error response to JSON
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorAttributes);
        } catch (JsonProcessingException e) {
            log.error("Error serializing error response", e);
            bytes = "{\"error\":\"Internal server error\"}".getBytes(StandardCharsets.UTF_8);
        }

        // Write error response to client
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    /**
     * Determine appropriate HTTP status code based on exception type
     * FIXED: Properly handles HttpStatusCode to HttpStatus conversion
     */
    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            HttpStatusCode statusCode = ((ResponseStatusException) ex).getStatusCode();
            // Convert HttpStatusCode to HttpStatus
            return HttpStatus.resolve(statusCode.value());
        } else if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof org.springframework.web.server.ServerWebInputException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex.getMessage() != null && ex.getMessage().contains("404")) {
            return HttpStatus.NOT_FOUND;
        } else if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * Determine user-friendly error message based on exception
     */
    private String determineErrorMessage(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            String reason = ((ResponseStatusException) ex).getReason();
            return reason != null ? reason : "Request failed";
        } else if (ex instanceof IllegalArgumentException) {
            return "Invalid request: " + ex.getMessage();
        } else if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) {
            return "Service temporarily unavailable. Please try again later.";
        } else if (ex.getMessage() != null && ex.getMessage().contains("404")) {
            return "Requested resource not found";
        } else {
            // Don't expose internal error details to clients
            return "An unexpected error occurred. Please contact support if the issue persists.";
        }
    }

    /**
     * Build standardized error response map
     */
    private Map<String, Object> buildErrorResponse(
            ServerWebExchange exchange,
            HttpStatus status,
            String message,
            Throwable ex) {

        Map<String, Object> errorAttributes = new HashMap<>();

        // Standard error fields
        errorAttributes.put("timestamp", LocalDateTime.now().toString());
        errorAttributes.put("status", status.value());
        errorAttributes.put("error", status.getReasonPhrase());
        errorAttributes.put("message", message);
        errorAttributes.put("path", exchange.getRequest().getPath().value());

        // Add request ID if present (for tracing)
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
        if (requestId != null) {
            errorAttributes.put("requestId", requestId);
        }

        // In development, include exception class name for debugging
        // Remove this in production for security
        if (log.isDebugEnabled()) {
            errorAttributes.put("exceptionType", ex.getClass().getSimpleName());
        }

        return errorAttributes;
    }
}