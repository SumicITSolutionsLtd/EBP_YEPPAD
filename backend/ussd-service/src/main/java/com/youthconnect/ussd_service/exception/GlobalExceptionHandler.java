package com.youthconnect.ussd_service.exception;

import com.youthconnect.ussd_service.config.MonitoringConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.validation.FieldError; // ✓ FIXED: Correct import

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the USSD service.
 *
 * @author YouthConnect Uganda Development Team
 * @version 2.0.0
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MonitoringConfig monitoringConfig;

    /**
     * Handles USSD security exceptions.
     */
    @ExceptionHandler(UssdSecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(
            UssdSecurityException ex, WebRequest request, HttpServletRequest httpRequest) {

        log.error("Security violation detected: {} | Client: {} | Event: {}",
                ex.getMessage(), ex.getClientInfo(), ex.getSecurityEvent(), ex);

        // ✓ FIXED: Use helper method
        monitoringConfig.recordSecurityEvent(
                ex.getSecurityEvent() != null ? ex.getSecurityEvent() : "unknown",
                "high",
                ex.getClientInfo() != null ? ex.getClientInfo() : "unknown"
        );

        Map<String, Object> errorResponse = createErrorResponse(
                "SECURITY_VIOLATION",
                "Request cannot be processed due to security constraints",
                HttpStatus.FORBIDDEN
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        log.warn("Request validation failed: {}", ex.getMessage());

        // ✓ FIXED: Correct import usage
        Map<String, String> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null
                                ? fieldError.getDefaultMessage()
                                : "Validation failed"
                ));

        Map<String, Object> errorResponse = createErrorResponse(
                "VALIDATION_ERROR",
                "Request validation failed",
                HttpStatus.BAD_REQUEST
        );
        errorResponse.put("validationErrors", validationErrors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles constraint violation exceptions.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {

        log.warn("Constraint validation failed: {}", ex.getMessage());

        Map<String, String> violations = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        Map<String, Object> errorResponse = createErrorResponse(
                "CONSTRAINT_VIOLATION",
                "Request contains invalid data",
                HttpStatus.BAD_REQUEST
        );
        errorResponse.put("violations", violations);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles generic exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {

        log.error("Unhandled exception occurred: {}", ex.getMessage(), ex);

        Map<String, Object> errorResponse = createErrorResponse(
                "INTERNAL_ERROR",
                "A system error occurred. Please contact support if the problem persists.",
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Creates standardized error response.
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message, HttpStatus httpStatus) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", httpStatus.value());
        errorResponse.put("ussdResponse", "END " + message);

        return errorResponse;
    }
}