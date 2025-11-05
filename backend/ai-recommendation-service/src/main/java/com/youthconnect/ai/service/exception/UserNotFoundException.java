package com.youthconnect.ai.service.exception;

/**
 * Exception thrown when user profile cannot be found
 *
 * @author Douglas Kings Kato
 */
public class UserNotFoundException extends AIRecommendationException {

    public UserNotFoundException(Long userId) {
        super("USER_NOT_FOUND", "User not found with ID: " + userId);
    }

    public UserNotFoundException(String message) {
        super("USER_NOT_FOUND", message);
    }
}