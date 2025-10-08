package com.youthconnect.user_service.client;

import com.youthconnect.user_service.dto.request.UserActivityRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class AIRecommendationServiceFallback implements AIRecommendationServiceClient {

    @Override
    public ResponseEntity<Map<String, Object>> recordUserActivity(UserActivityRequest request) {
        log.debug("AI service unavailable - activity recording fallback for user: {}",
                request.getUserId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Activity logged locally (AI service unavailable)",
                "fallback", true
        ));
    }

    @Override
    public ResponseEntity<Map<String, Object>> getOpportunityRecommendations(Long userId, int limit) {
        log.debug("AI service unavailable - using default recommendations for user: {}", userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "recommendations", Map.of(), // Empty recommendations
                "message", "Default recommendations (AI service unavailable)",
                "fallback", true
        ));
    }

    @Override
    public ResponseEntity<Map<String, Object>> getUserBehaviorInsights(Long userId) {
        log.debug("AI service unavailable - behavior insights fallback for user: {}", userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "insights", Map.of("message", "Insights unavailable"),
                "fallback", true
        ));
    }
}