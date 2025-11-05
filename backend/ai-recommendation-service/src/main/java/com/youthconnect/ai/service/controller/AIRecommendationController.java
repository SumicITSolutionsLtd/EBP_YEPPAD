package com.youthconnect.ai.service.controller;

import com.youthconnect.ai.service.dto.*;
import com.youthconnect.ai.service.service.AIRecommendationService;
import com.youthconnect.ai.service.service.UserActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API Controller for AI Recommendation Service
 * Exposes intelligent recommendation endpoints
 *
 * Base Path: /api/v1/ai
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIRecommendationController {

    private final AIRecommendationService aiService;
    private final UserActivityService activityService;

    /**
     * Get personalized opportunity recommendations for a user
     *
     * GET /api/v1/ai/recommendations/opportunities/{userId}
     *
     * @param userId User ID
     * @param limit Maximum number of recommendations (default: 10)
     * @return List of personalized opportunity recommendations
     */
    @GetMapping("/recommendations/opportunities/{userId}")
    public ResponseEntity<Map<String, Object>> getOpportunityRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Getting opportunity recommendations for user: {}", userId);

        try {
            List<OpportunityRecommendation> recommendations =
                    aiService.getPersonalizedOpportunities(userId, limit);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "userId", userId,
                    "recommendations", recommendations,
                    "count", recommendations.size(),
                    "message", "Recommendations generated successfully"
            ));

        } catch (Exception e) {
            log.error("Error generating opportunity recommendations for user {}: {}",
                    userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to generate recommendations",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get personalized learning content recommendations
     *
     * GET /api/v1/ai/recommendations/content/{userId}
     *
     * @param userId User ID
     * @param limit Maximum number of recommendations (default: 8)
     * @return List of personalized content recommendations
     */
    @GetMapping("/recommendations/content/{userId}")
    public ResponseEntity<Map<String, Object>> getContentRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "8") int limit) {

        log.info("Getting content recommendations for user: {}", userId);

        try {
            List<ContentRecommendation> recommendations =
                    aiService.getPersonalizedContent(userId, limit);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "userId", userId,
                    "recommendations", recommendations,
                    "count", recommendations.size()
            ));

        } catch (Exception e) {
            log.error("Error generating content recommendations for user {}: {}",
                    userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to generate content recommendations"
            ));
        }
    }

    /**
     * Get compatible mentors for a youth user
     *
     * GET /api/v1/ai/recommendations/mentors/{youthUserId}
     *
     * @param youthUserId Youth user ID
     * @param limit Maximum number of recommendations (default: 5)
     * @return List of compatible mentor recommendations
     */
    @GetMapping("/recommendations/mentors/{youthUserId}")
    public ResponseEntity<Map<String, Object>> getMentorRecommendations(
            @PathVariable Long youthUserId,
            @RequestParam(defaultValue = "5") int limit) {

        log.info("Getting mentor recommendations for youth user: {}", youthUserId);

        try {
            List<MentorRecommendation> recommendations =
                    aiService.getCompatibleMentors(youthUserId, limit);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "youthUserId", youthUserId,
                    "mentorRecommendations", recommendations,
                    "count", recommendations.size()
            ));

        } catch (Exception e) {
            log.error("Error generating mentor recommendations for user {}: {}",
                    youthUserId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to generate mentor recommendations"
            ));
        }
    }

    /**
     * Predict success probability for user applying to opportunity
     *
     * GET /api/v1/ai/predict/success/{userId}/{opportunityId}
     *
     * @param userId User ID
     * @param opportunityId Opportunity ID
     * @return Success probability and confidence level
     */
    @GetMapping("/predict/success/{userId}/{opportunityId}")
    public ResponseEntity<Map<String, Object>> predictSuccessProbability(
            @PathVariable Long userId,
            @PathVariable Long opportunityId) {

        log.info("Predicting success probability for user {} applying to opportunity {}",
                userId, opportunityId);

        try {
            double probability = aiService.predictSuccessProbability(userId, opportunityId);
            String confidenceLevel = getConfidenceLevel(probability);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "userId", userId,
                    "opportunityId", opportunityId,
                    "successProbability", probability,
                    "confidenceLevel", confidenceLevel,
                    "recommendation", generateApplicationAdvice(probability)
            ));

        } catch (Exception e) {
            log.error("Error predicting success probability: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to predict success probability"
            ));
        }
    }

    /**
     * Record user activity for learning model
     *
     * POST /api/v1/ai/activity/record
     *
     * @param request Activity details
     * @return Confirmation response
     */
    @PostMapping("/activity/record")
    public ResponseEntity<Map<String, Object>> recordUserActivity(
            @Valid @RequestBody UserActivityRequest request) {

        log.info("Recording user activity - User: {}, Activity: {}",
                request.getUserId(), request.getActivityType());

        try {
            activityService.recordActivity(
                    request.getUserId(),
                    request.getActivityType(),
                    request.getTargetId(),
                    request.getMetadata()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Activity recorded successfully"
            ));

        } catch (Exception e) {
            log.error("Error recording user activity: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to record activity"
            ));
        }
    }

    /**
     * Get user behavior insights
     *
     * GET /api/v1/ai/insights/behavior/{userId}
     *
     * @param userId User ID
     * @return Behavioral insights and analytics
     */
    @GetMapping("/insights/behavior/{userId}")
    public ResponseEntity<Map<String, Object>> getUserBehaviorInsights(
            @PathVariable Long userId) {

        log.info("Getting behavior insights for user: {}", userId);

        try {
            UserBehaviorInsights insights = aiService.getUserBehaviorInsights(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "userId", userId,
                    "insights", insights
            ));

        } catch (Exception e) {
            log.error("Error getting behavior insights for user {}: {}",
                    userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to get behavior insights"
            ));
        }
    }

    /**
     * Health check endpoint
     *
     * GET /api/v1/ai/health
     *
     * @return Service health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        boolean mlModelsHealthy = aiService.checkMLModelsHealth();
        boolean dataServiceHealthy = aiService.checkDataServiceHealth();

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "ai-recommendation-service",
                "checks", Map.of(
                        "mlModels", mlModelsHealthy ? "UP" : "DOWN",
                        "dataService", dataServiceHealthy ? "UP" : "DOWN"
                )
        ));
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================

    /**
     * Determine confidence level based on probability score
     */
    private String getConfidenceLevel(double probability) {
        if (probability >= 0.8) return "HIGH";
        if (probability >= 0.6) return "MEDIUM";
        if (probability >= 0.4) return "MODERATE";
        return "LOW";
    }

    /**
     * Generate application advice based on success probability
     */
    private String generateApplicationAdvice(double probability) {
        if (probability >= 0.8) {
            return "Excellent match! Your profile strongly aligns with this opportunity.";
        } else if (probability >= 0.6) {
            return "Good match! Consider highlighting relevant experience in your application.";
        } else if (probability >= 0.4) {
            return "Moderate match. Focus on demonstrating your commitment and learning ability.";
        } else {
            return "Consider gaining more relevant experience before applying.";
        }
    }
}