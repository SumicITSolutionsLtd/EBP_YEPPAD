package com.youthconnect.job_services.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * AI Recommendation Service Client
 *
 * Feign client for communication with ai-recommendation-service.
 * Used to fetch personalized job recommendations and track user activity.
 *
 * @author Douglas Kings Kato
 * @since 1.0.0
 */
@FeignClient(
        name = "ai-recommendation-service",
        path = "/api/v1/ai",
        fallbackFactory = AIRecommendationClientFallback.class
)
public interface AIRecommendationClient {

    /**
     * Get personalized job recommendations for a user
     *
     * @param userId The user ID
     * @param limit Maximum number of recommendations
     * @return List of recommended jobs with match scores
     */
    @GetMapping("/recommendations/jobs/{userId}")
    List<RecommendedJobDto> getJobRecommendations(
            @PathVariable("userId") UUID userId,
            @RequestParam("limit") int limit
    );

    /**
     * Record job view activity for recommendation algorithm
     *
     * @param userId User who viewed the job
     * @param jobId Job that was viewed
     */
    @PostMapping("/activity/job-viewed")
    void recordJobView(
            @RequestParam("userId") UUID userId,
            @RequestParam("jobId") UUID jobId
    );

    /**
     * Record job application activity
     *
     * @param userId User who applied
     * @param jobId Job applied to
     */
    @PostMapping("/activity/job-applied")
    void recordJobApplication(
            @RequestParam("userId") UUID userId,
            @RequestParam("jobId") UUID jobId
    );

    /**
     * Get job match score for a specific user
     *
     * @param userId User ID
     * @param jobId Job ID
     * @return Match score (0-100)
     */
    @GetMapping("/match-score")
    Double getJobMatchScore(
            @RequestParam("userId") UUID userId,
            @RequestParam("jobId") UUID jobId
    );

    /**
     * Initialize user preferences when they register
     *
     * @param userId User ID
     * @param interests List of user interests
     */
    @PostMapping("/initialize-preferences")
    void initializeUserPreferences(
            @RequestParam("userId") UUID userId,
            @RequestBody List<String> interests
    );

    /**
     * DTOs for AI Recommendation responses
     */
    record RecommendedJobDto(
            UUID jobId,
            Double matchScore,
            String matchReason,
            List<String> matchingSkills
    ) {}
}