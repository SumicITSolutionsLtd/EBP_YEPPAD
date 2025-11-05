package com.youthconnect.job_services.exception;

/**
 * Invalid File Exception
 *
 * Thrown when uploaded file fails validation (type, size, content).
 * HTTP Status: 400 BAD REQUEST
 *
 * @author Douglas Kings Kato
 * @since 1.0.0
 */
public class InvalidFileException extends RuntimeException {

    /**
     * Constructor with custom message
     *
     * @param message Error message
     */
    public InvalidFileException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     *
     * @param message Error message
     * @param cause Root cause
     */
    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
