package com.youthconnect.opportunity_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;

/**
 * Feign client for sending analytics events to Analytics Service
 * Tracks user behavior, opportunity performance, and system metrics
 */
@FeignClient(name = "analytics-service", path = "/api/analytics")
public interface AnalyticsServiceClient {

    /**
     * Track opportunity creation event
     */
    @PostMapping("/track/opportunity-created")
    void trackOpportunityCreated(
            @RequestParam("opportunityId") Long opportunityId,
            @RequestParam("postedById") Long postedById,
            @RequestParam("type") String type,
            @RequestParam("timestamp") LocalDateTime timestamp
    );

    /**
     * Track application submission event
     */
    @PostMapping("/track/application-submitted")
    void trackApplicationSubmitted(
            @RequestParam("applicationId") Long applicationId,
            @RequestParam("opportunityId") Long opportunityId,
            @RequestParam("applicantId") Long applicantId,
            @RequestParam("timestamp") LocalDateTime timestamp
    );

    /**
     * Track opportunity view event
     */
    @PostMapping("/track/opportunity-viewed")
    void trackOpportunityViewed(
            @RequestParam("opportunityId") Long opportunityId,
            @RequestParam("userId") Long userId,
            @RequestParam("timestamp") LocalDateTime timestamp
    );

    /**
     * Track search activity
     */
    @PostMapping("/track/opportunity-search")
    void trackOpportunitySearch(
            @RequestParam("userId") Long userId,
            @RequestParam("searchTerm") String searchTerm,
            @RequestParam("resultsCount") int resultsCount,
            @RequestParam("timestamp") LocalDateTime timestamp
    );
}