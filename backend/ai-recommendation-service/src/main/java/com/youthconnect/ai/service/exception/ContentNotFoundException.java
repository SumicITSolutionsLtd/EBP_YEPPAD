package com.youthconnect.ai.service.exception;

/**
 * Exception thrown when learning content cannot be found
 *
 * @author Douglas Kings Kato
 */
public class ContentNotFoundException extends AIRecommendationException {

    public ContentNotFoundException(Long contentId) {
        super("CONTENT_NOT_FOUND", "Learning content not found with ID: " + contentId);
    }

    public ContentNotFoundException(String message) {
        super("CONTENT_NOT_FOUND", message);
    }
}