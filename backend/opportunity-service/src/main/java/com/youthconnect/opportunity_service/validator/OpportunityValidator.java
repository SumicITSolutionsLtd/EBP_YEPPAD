package com.youthconnect.opportunity_service.validator;

import com.youthconnect.opportunity_service.dto.OpportunityRequest;
import com.youthconnect.opportunity_service.exception.InvalidApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * Validator for opportunity-related business rules
 */
@Component
@Slf4j
public class OpportunityValidator {

    /**
     * Validate opportunity creation request
     */
    public void validateOpportunityRequest(OpportunityRequest request) {
        log.debug("Validating opportunity request: {}", request.getTitle());

        // Validate title
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new InvalidApplicationException("Opportunity title is required");
        }

        if (request.getTitle().length() < 10 || request.getTitle().length() > 255) {
            throw new InvalidApplicationException(
                    "Opportunity title must be between 10 and 255 characters");
        }

        // Validate description
        if (request.getDescription() != null && request.getDescription().length() > 5000) {
            throw new InvalidApplicationException(
                    "Description cannot exceed 5000 characters");
        }

        // Validate deadline is in the future
        if (request.getApplicationDeadline() != null) {
            if (request.getApplicationDeadline().isBefore(LocalDateTime.now())) {
                throw new InvalidApplicationException(
                        "Application deadline must be in the future");
            }

            // Deadline should be reasonable (not more than 2 years in future)
            if (request.getApplicationDeadline().isAfter(LocalDateTime.now().plusYears(2))) {
                throw new InvalidApplicationException(
                        "Application deadline cannot be more than 2 years in the future");
            }
        }

        log.debug("Opportunity request validation passed");
    }

    /**
     * Validate opportunity update
     */
    public void validateOpportunityUpdate(OpportunityRequest request, Long opportunityId) {
        log.debug("Validating opportunity update for ID: {}", opportunityId);

        if (opportunityId == null || opportunityId <= 0) {
            throw new InvalidApplicationException("Invalid opportunity ID");
        }

        validateOpportunityRequest(request);
    }
}