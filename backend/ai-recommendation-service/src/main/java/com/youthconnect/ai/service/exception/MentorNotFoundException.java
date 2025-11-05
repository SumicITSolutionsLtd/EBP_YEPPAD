package com.youthconnect.ai.service.exception;

/**
 * Exception thrown when mentor cannot be found
 *
 * @author Douglas Kings Kato
 */
public class MentorNotFoundException extends AIRecommendationException {

    public MentorNotFoundException(Long mentorId) {
        super("MENTOR_NOT_FOUND", "Mentor not found with ID: " + mentorId);
    }

    public MentorNotFoundException(String message) {
        super("MENTOR_NOT_FOUND", message);
    }
}