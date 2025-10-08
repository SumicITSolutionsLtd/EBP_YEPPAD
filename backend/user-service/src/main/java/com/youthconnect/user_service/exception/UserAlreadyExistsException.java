// UserAlreadyExistsException.java
package com.youthconnect.user_service.exception;

/**
 * Exception thrown when attempting to register a user that already exists
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}