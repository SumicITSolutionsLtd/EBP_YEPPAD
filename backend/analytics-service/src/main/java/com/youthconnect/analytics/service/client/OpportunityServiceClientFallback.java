package com.youthconnect.analytics.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Fallback for Analytics service Opportunity Service Client
 */
@Slf4j
@Component
public class OpportunityServiceClientFallback implements OpportunityServiceClient {

    @Override
    public ResponseEntity<Map<String, Object>> getOpportunityStatistics() {
        log.warn("Opportunity service unavailable - using fallback statistics");
        return ResponseEntity.ok(Map.of(
                "totalOpportunities", 89,
                "activeOpportunities", 23,
                "newOpportunities", 12,
                "fallback", true
        ));
    }

    @Override
    public ResponseEntity<Map<String, Object>> getApplicationStatistics() {
        log.warn("Opportunity service unavailable - using fallback application statistics");
        return ResponseEntity.ok(Map.of(
                "totalApplications", 567,
                "pendingApplications", 123,
                "approvedApplications", 234,
                "fallback", true
        ));
    }

    @Override
    public ResponseEntity<Map<String, Object>> getNgoOpportunityPerformance(Long ngoId) {
        log.warn("Opportunity service unavailable - using fallback NGO performance for: {}", ngoId);
        return ResponseEntity.ok(Map.of(
                "ngoId", ngoId,
                "opportunities", 15,
                "applications", 87,
                "approved", 23,
                "fallback", true
        ));
    }

    @Override
    public ResponseEntity<Map<String, Object>> checkHealth() {
        return ResponseEntity.ok(Map.of("status", "DOWN", "fallback", true));
    }
}
