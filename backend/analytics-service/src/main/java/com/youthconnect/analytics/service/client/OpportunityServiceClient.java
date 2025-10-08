package com.youthconnect.analytics.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Feign client for Analytics service to get opportunity data
 */
@FeignClient(name = "opportunity-service", fallback = OpportunityServiceClientFallback.class)
public interface OpportunityServiceClient {

    @GetMapping("/api/opportunities/stats")
    ResponseEntity<Map<String, Object>> getOpportunityStatistics();

    @GetMapping("/api/opportunities/applications/stats")
    ResponseEntity<Map<String, Object>> getApplicationStatistics();

    @GetMapping("/api/opportunities/ngo/{ngoId}/performance")
    ResponseEntity<Map<String, Object>> getNgoOpportunityPerformance(@PathVariable Long ngoId);

    @GetMapping("/actuator/health")
    ResponseEntity<Map<String, Object>> checkHealth();
}
