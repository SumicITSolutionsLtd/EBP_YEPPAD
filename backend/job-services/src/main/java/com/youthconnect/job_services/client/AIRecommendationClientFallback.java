package com.youthconnect.job_services.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * AI Recommendation Client Fallback
 *
 * Provides fallback responses when ai-recommendation-service is unavailable.
 * Returns empty recommendations instead of failing the entire request.
 *
 * @author Douglas Kings Kato
 * @since 1.0.0
 */
@Slf4j
@Component
public class AIRecommendationClientFallback implements FallbackFactory<AIRecommendationClient> {

    @Override
    public AIRecommendationClient create(Throwable cause) {
        return new AIRecommendationClient() {

            @Override
            public List<RecommendedJobDto> getJobRecommendations(UUID userId, int limit) {
                log.error("AI recommendation service unavailable for getJobRecommendations(userId: {}, limit: {}). Cause: {}. Returning empty list.",
                        userId, limit, cause.getMessage());
                // Return empty list - calling service will use fallback logic
                return Collections.emptyList();
            }

            @Override
            public void recordJobView(UUID userId, UUID jobId) {
                log.warn("AI recommendation service unavailable for recordJobView(userId: {}, jobId: {}). Cause: {}. Activity not tracked.",
                        userId, jobId, cause.getMessage());
                // Silent failure - activity tracking is non-critical
            }

            @Override
            public void recordJobApplication(UUID userId, UUID jobId) {
                log.warn("AI recommendation service unavailable for recordJobApplication(userId: {}, jobId: {}). Cause: {}. Activity not tracked.",
                        userId, jobId, cause.getMessage());
                // Silent failure - activity tracking is non-critical
            }

            @Override
            public Double getJobMatchScore(UUID userId, UUID jobId) {
                log.error("AI recommendation service unavailable for getJobMatchScore(userId: {}, jobId: {}). Cause: {}. Returning default score.",
                        userId, jobId, cause.getMessage());
                return 50.0; // Default neutral score
            }

            @Override
            public void initializeUserPreferences(UUID userId, List<String> interests) {
                log.warn("AI recommendation service unavailable for initializeUserPreferences(userId: {}). Cause: {}. Preferences not initialized.",
                        userId, cause.getMessage());
                // Silent failure - can be initialized later
            }
        };
    }
}