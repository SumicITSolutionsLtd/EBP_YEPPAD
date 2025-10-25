package com.youthconnect.content_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign Client for Analytics Service Integration
 *
 * Tracks content engagement metrics for analytics dashboards
 *
 * @author Douglas Kings Kato
 * @version 1.0
 */
@FeignClient(
        name = "analytics-service",
        path = "/api/analytics",
        fallback = AnalyticsServiceClientFallback.class
)
public interface AnalyticsServiceClient {

    /**
     * Track when a user views a learning module
     *
     * @param moduleId Module that was viewed
     * @param userId User who viewed it
     */
    @PostMapping("/events/module-viewed")
    void trackModuleView(
            @RequestParam Long moduleId,
            @RequestParam Long userId
    );

    /**
     * Track when a user completes a learning module
     *
     * @param moduleId Module that was completed
     * @param userId User who completed it
     * @param timeSpentSeconds Total time spent on module
     */
    @PostMapping("/events/module-completed")
    void trackModuleCompletion(
            @RequestParam Long moduleId,
            @RequestParam Long userId,
            @RequestParam Integer timeSpentSeconds
    );

    /**
     * Track when a user views a community post
     *
     * @param postId Post that was viewed
     * @param userId User who viewed it
     */
    @PostMapping("/events/post-viewed")
    void trackPostView(
            @RequestParam Long postId,
            @RequestParam Long userId
    );

    /**
     * Track user engagement with content (comments, votes)
     *
     * @param contentType Type of content (POST, COMMENT)
     * @param contentId ID of the content
     * @param userId User who engaged
     * @param engagementType Type of engagement (COMMENT, UPVOTE, DOWNVOTE)
     */
    @PostMapping("/events/content-engagement")
    void trackContentEngagement(
            @RequestParam String contentType,
            @RequestParam Long contentId,
            @RequestParam Long userId,
            @RequestParam String engagementType
    );
}

/**
 * Fallback implementation for when Analytics Service is unavailable
 * Logs errors but doesn't fail the primary operation
 */
@Component
class AnalyticsServiceClientFallback implements AnalyticsServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceClientFallback.class);

    @Override
    public void trackModuleView(Long moduleId, Long userId) {
        log.warn("Analytics service unavailable - Module view not tracked: moduleId={}, userId={}",
                moduleId, userId);
    }

    @Override
    public void trackModuleCompletion(Long moduleId, Long userId, Integer timeSpentSeconds) {
        log.warn("Analytics service unavailable - Module completion not tracked: moduleId={}, userId={}",
                moduleId, userId);
    }

    @Override
    public void trackPostView(Long postId, Long userId) {
        log.warn("Analytics service unavailable - Post view not tracked: postId={}, userId={}",
                postId, userId);
    }

    @Override
    public void trackContentEngagement(String contentType, Long contentId, Long userId, String engagementType) {
        log.warn("Analytics service unavailable - Engagement not tracked: {}={}, userId={}, type={}",
                contentType, contentId, userId, engagementType);
    }
}