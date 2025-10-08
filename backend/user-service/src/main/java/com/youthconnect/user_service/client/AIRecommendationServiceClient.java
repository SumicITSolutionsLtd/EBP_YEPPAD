package com.youthconnect.user_service.client;

import com.youthconnect.user_service.dto.request.UserActivityRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Client for integrating with AI Recommendation Service
 * Handles user behavior tracking and personalized recommendations
 */
@FeignClient(name = "ai-recommendation-service", fallback = AIRecommendationServiceFallback.class)
public interface AIRecommendationServiceClient {

    @PostMapping("/api/ai/activity/record")
    ResponseEntity<Map<String, Object>> recordUserActivity(@RequestBody UserActivityRequest request);

    @GetMapping("/api/ai/recommendations/opportunities/{userId}")
    ResponseEntity<Map<String, Object>> getOpportunityRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit);

    @GetMapping("/api/ai/insights/behavior/{userId}")
    ResponseEntity<Map<String, Object>> getUserBehaviorInsights(@PathVariable Long userId);
}