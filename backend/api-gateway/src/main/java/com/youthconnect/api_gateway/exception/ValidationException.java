package com.youthconnect.api_gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Validation Exception for API Gateway
 *
 * Thrown when request validation fails (invalid parameters, malformed data, etc.).
 * This exception is ignored by circuit breakers since it represents client errors,
 * not service failures.
 *
 * HTTP Status: 400 Bad Request
 *
 * Usage Example:
 * <pre>
 * if (userId == null || userId.isEmpty()) {
 *     throw new ValidationException("User ID is required");
 * }
 * </pre>
 *
 * Location: api-gateway/src/main/java/com/youthconnect/api_gateway/exception/
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

    /**
     * Constructs a new validation exception with the specified detail message.
     *
     * @param message the detail message explaining the validation failure
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new validation exception with the specified detail message and cause.
     *
     * @param message the detail message explaining the validation failure
     * @param cause the cause of the validation failure
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new validation exception with a default message.
     * Used when no specific message is needed.
     */
    public ValidationException() {
        super("Invalid request data");
    }
}
