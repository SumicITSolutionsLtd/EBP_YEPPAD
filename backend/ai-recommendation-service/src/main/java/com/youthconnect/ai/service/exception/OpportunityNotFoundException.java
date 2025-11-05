package com.youthconnect.ai.service.exception;

/**
 * Exception thrown when opportunity cannot be found
 *
 * @author Douglas Kings Kato
 */
public class OpportunityNotFoundException extends AIRecommendationException {

    public OpportunityNotFoundException(Long opportunityId) {
        super("OPPORTUNITY_NOT_FOUND", "Opportunity not found with ID: " + opportunityId);
    }

    public OpportunityNotFoundException(String message) {
        super("OPPORTUNITY_NOT_FOUND", message);
    }
}