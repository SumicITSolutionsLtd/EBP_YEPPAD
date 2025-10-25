package com.youthconnect.opportunity_service.service;

import com.youthconnect.opportunity_service.client.AnalyticsServiceClient;
import com.youthconnect.opportunity_service.event.OpportunityPublishedEvent;
import com.youthconnect.opportunity_service.event.ApplicationSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for handling and publishing domain events.
 * Enables event-driven architecture and analytics tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpportunityEventService {

    private final AnalyticsServiceClient analyticsServiceClient;

    /**
     * Handle opportunity published events
     * Sends analytics data to analytics service
     */
    @Async("analyticsExecutor")
    @EventListener
    public void handleOpportunityPublished(OpportunityPublishedEvent event) {
        try {
            log.info("Processing opportunity published event for ID: {}",
                    event.getOpportunityId());

            // Send to analytics service for tracking
            analyticsServiceClient.trackOpportunityCreated(
                    event.getOpportunityId(),
                    event.getPostedById(),
                    event.getOpportunityType(),
                    event.getTimestamp()
            );

            log.debug("Opportunity published event processed successfully");
        } catch (Exception e) {
            log.error("Failed to process opportunity published event", e);
        }
    }

    /**
     * Handle application submitted events
     * Tracks user engagement and application metrics
     */
    @Async("analyticsExecutor")
    @EventListener
    public void handleApplicationSubmitted(ApplicationSubmittedEvent event) {
        try {
            log.info("Processing application submitted event for opportunity: {}",
                    event.getOpportunityId());

            // Send to analytics service
            analyticsServiceClient.trackApplicationSubmitted(
                    event.getApplicationId(),
                    event.getOpportunityId(),
                    event.getApplicantId(),
                    event.getTimestamp()
            );

            log.debug("Application submitted event processed successfully");
        } catch (Exception e) {
            log.error("Failed to process application submitted event", e);
        }
    }
}