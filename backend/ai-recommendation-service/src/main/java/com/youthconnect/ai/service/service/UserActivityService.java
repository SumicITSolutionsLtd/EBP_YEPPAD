package com.youthconnect.ai.service.service;

import com.youthconnect.ai.service.entity.UserActivityLog;
import com.youthconnect.ai.service.model.UserBehaviorData;
import com.youthconnect.ai.service.repository.UserActivityLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * User Activity Tracking Service
 *
 * Records and analyzes user behavior for AI recommendations
 * Handles activity logging, behavior analysis, and engagement metrics
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserActivityService {

    private final UserActivityLogRepository activityRepository;
    private final ObjectMapper objectMapper;

    /**
     * Record user activity asynchronously for performance
     * This method is called from other services via Feign clients
     *
     * @param userId The user performing the activity
     * @param activityType Type of activity (VIEW_OPPORTUNITY, LISTEN_AUDIO, etc.)
     * @param targetId ID of the target item (optional)
     * @param metadata Additional context information
     */
    @Async
    public CompletableFuture<Void> recordActivity(Long userId, String activityType,
                                                  Long targetId, Map<String, Object> metadata) {
        try {
            log.debug("Recording activity for user {}: {} on target {}", userId, activityType, targetId);

            // Generate session ID if not provided in metadata
            String sessionId = extractSessionId(metadata);

            // Convert metadata to JSON string for storage
            String metadataJson = convertMetadataToJson(metadata);

            // Create activity log entry
            UserActivityLog activityLog = UserActivityLog.builder()
                    .userId(userId)
                    .activityType(activityType)
                    .targetId(targetId)
                    .targetType(extractTargetType(activityType))
                    .sessionId(sessionId)
                    .metadata(metadataJson)
                    .createdAt(LocalDateTime.now())
                    .build();

            // Save to database
            activityRepository.save(activityLog);

            log.debug("Successfully recorded activity for user {}: {}", userId, activityType);

        } catch (Exception e) {
            log.error("Failed to record activity for user {}: {} - {}", userId, activityType, e.getMessage(), e);
            // Don't throw exception to prevent disrupting main business flow
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Get user behavior analysis for recommendation algorithms
     *
     * @param userId User to analyze
     * @param days Number of days to look back
     * @return Behavior analysis data
     */
    public Map<String, Object> getUserBehaviorAnalysis(Long userId, int days) {
        log.debug("Analyzing user behavior for user: {} over {} days", userId, days);

        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);

            // Get recent activities
            List<UserActivityLog> recentActivities = activityRepository.findRecentActivitiesByUser(userId, since);

            // Calculate behavior metrics
            Map<String, Object> analysis = new HashMap<>();
            analysis.put("userId", userId);
            analysis.put("analysisSpan", days);
            analysis.put("totalActivities", recentActivities.size());
            analysis.put("sessionCount", calculateSessionCount(userId, since));
            analysis.put("averageSessionDuration", calculateAverageSessionDuration(recentActivities));
            analysis.put("mostActiveHours", getMostActiveHours(userId));
            analysis.put("preferredContentTypes", getPreferredContentTypes(userId));
            analysis.put("engagementLevel", calculateEngagementLevel(recentActivities));
            analysis.put("topInterests", extractTopInterests(recentActivities));
            analysis.put("lastActivity", getLastActivity(recentActivities));

            log.debug("Completed behavior analysis for user: {}", userId);
            return analysis;

        } catch (Exception e) {
            log.error("Error analyzing user behavior for user {}: {}", userId, e.getMessage(), e);
            return createFallbackBehaviorAnalysis(userId);
        }
    }

    /**
     * Get user behavior data for recommendation algorithms
     *
     * CRITICAL METHOD: Called by AIRecommendationServiceImpl
     *
     * @param userId User to analyze
     * @param days Number of days to look back
     * @return UserBehaviorData model for AI processing
     */
    public UserBehaviorData getUserBehaviorData(Long userId, int days) {
        log.debug("Getting behavior data for user: {} over {} days", userId, days);

        try {
            LocalDateTime since = LocalDateTime.now().minusDays(days);

            // Get recent activities
            List<UserActivityLog> recentActivities = activityRepository.findRecentActivitiesByUser(userId, since);

            // Calculate session count
            Long sessionCount = calculateSessionCount(userId, since);

            // Calculate average session duration
            double avgSessionDuration = calculateAverageSessionDuration(recentActivities);

            // Get most active hours
            List<Integer> mostActiveHours = getMostActiveHours(userId);

            // Get preferred content types
            List<String> preferredTypes = getPreferredContentTypes(userId);

            // Calculate engagement metrics
            String engagementLevel = calculateEngagementLevel(recentActivities);

            // Count activity types
            int opportunityViews = countActivityType(recentActivities, "VIEW_OPPORTUNITY");
            int applicationsSubmitted = countActivityType(recentActivities, "APPLY_JOB");
            int learningModulesAccessed = countActivityType(recentActivities, "LISTEN_AUDIO");
            int communityPostViews = countActivityType(recentActivities, "VIEW_POST");

            // Build behavior data model
            return UserBehaviorData.builder()
                    .userId(userId)
                    .sessionCount(sessionCount != null ? sessionCount.intValue() : 0)
                    .averageSessionDuration(avgSessionDuration)
                    .mostActiveHours(mostActiveHours)
                    .preferredContentTypes(preferredTypes)
                    .totalInteractions(recentActivities.size())
                    .opportunityViews(opportunityViews)
                    .applicationsSubmitted(applicationsSubmitted)
                    .learningModulesAccessed(learningModulesAccessed)
                    .communityPostViews(communityPostViews)
                    .applicationSuccessRate(calculateSuccessRate(userId))
                    .lastInteractionTime(getLastInteractionTime(recentActivities))
                    .engagementLevel(engagementLevel)
                    .build();

        } catch (Exception e) {
            log.error("Error getting behavior data for user {}: {}", userId, e.getMessage(), e);
            return createFallbackBehaviorDataModel(userId);
        }
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================

    private String extractSessionId(Map<String, Object> metadata) {
        if (metadata != null && metadata.containsKey("sessionId")) {
            return metadata.get("sessionId").toString();
        }
        return UUID.randomUUID().toString();
    }

    private String extractTargetType(String activityType) {
        if (activityType.contains("OPPORTUNITY")) return "OPPORTUNITY";
        if (activityType.contains("AUDIO") || activityType.contains("MODULE")) return "LEARNING_MODULE";
        if (activityType.contains("MENTOR")) return "MENTOR";
        if (activityType.contains("POST")) return "COMMUNITY_POST";
        if (activityType.contains("JOB")) return "JOB";
        return "UNKNOWN";
    }

    private String convertMetadataToJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return "{}";
        }

        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            log.warn("Failed to convert metadata to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    private Long calculateSessionCount(Long userId, LocalDateTime since) {
        try {
            return activityRepository.countDistinctSessionsByUser(userId, since);
        } catch (Exception e) {
            log.warn("Error calculating session count for user {}: {}", userId, e.getMessage());
            return 0L;
        }
    }

    private double calculateAverageSessionDuration(List<UserActivityLog> activities) {
        if (activities.isEmpty()) return 0.0;

        // Group activities by session and calculate durations
        Map<String, List<UserActivityLog>> sessionGroups = activities.stream()
                .collect(Collectors.groupingBy(UserActivityLog::getSessionId));

        double totalDuration = sessionGroups.values().stream()
                .mapToDouble(this::calculateSessionDuration)
                .sum();

        return totalDuration / sessionGroups.size();
    }

    private double calculateSessionDuration(List<UserActivityLog> sessionActivities) {
        if (sessionActivities.size() < 2) return 1.0; // Default 1 minute for single activity

        sessionActivities.sort(Comparator.comparing(UserActivityLog::getCreatedAt));

        LocalDateTime start = sessionActivities.get(0).getCreatedAt();
        LocalDateTime end = sessionActivities.get(sessionActivities.size() - 1).getCreatedAt();

        // Calculate duration in minutes
        return java.time.Duration.between(start, end).toMinutes() + 1.0;
    }

    private List<Integer> getMostActiveHours(Long userId) {
        try {
            List<Object[]> hourlyData = activityRepository.findUserActivityPatternByHour(userId);

            return hourlyData.stream()
                    .map(row -> (Integer) row[0])
                    .limit(5) // Top 5 most active hours
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Error getting active hours for user {}: {}", userId, e.getMessage());
            return Arrays.asList(9, 10, 14, 15, 20); // Default business hours
        }
    }

    private List<String> getPreferredContentTypes(Long userId) {
        try {
            List<Object[]> contentTypes = activityRepository.findMostPopularContentTypesByUser(userId);

            return contentTypes.stream()
                    .map(row -> (String) row[0])
                    .limit(3) // Top 3 content types
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Error getting content preferences for user {}: {}", userId, e.getMessage());
            return Arrays.asList("OPPORTUNITY", "LEARNING_MODULE", "COMMUNITY_POST");
        }
    }

    private String calculateEngagementLevel(List<UserActivityLog> activities) {
        int activityCount = activities.size();

        if (activityCount >= 100) return "HIGH";
        if (activityCount >= 50) return "MEDIUM";
        if (activityCount >= 20) return "LOW";
        return "NEW";
    }

    private List<String> extractTopInterests(List<UserActivityLog> activities) {
        // Analyze activities to extract interests
        Map<String, Long> interestCounts = activities.stream()
                .map(UserActivityLog::getTargetType)
                .collect(Collectors.groupingBy(
                        type -> type != null ? type : "GENERAL",
                        Collectors.counting()
                ));

        return interestCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private LocalDateTime getLastActivity(List<UserActivityLog> activities) {
        return activities.stream()
                .map(UserActivityLog::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private int countActivityType(List<UserActivityLog> activities, String activityType) {
        return (int) activities.stream()
                .filter(activity -> activityType.equals(activity.getActivityType()))
                .count();
    }

    private double calculateSuccessRate(Long userId) {
        // STUB: In production, query applications table for actual success rate
        return 0.65; // 65% default
    }

    private Long getLastInteractionTime(List<UserActivityLog> activities) {
        return activities.stream()
                .map(UserActivityLog::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .map(dt -> java.time.Instant.now().toEpochMilli())
                .orElse(null);
    }

    private Map<String, Object> createFallbackBehaviorAnalysis(Long userId) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("userId", userId);
        fallback.put("totalActivities", 0);
        fallback.put("sessionCount", 0L);
        fallback.put("engagementLevel", "NEW");
        fallback.put("error", "Analysis unavailable");
        return fallback;
    }

    private UserBehaviorData createFallbackBehaviorDataModel(Long userId) {
        return UserBehaviorData.builder()
                .userId(userId)
                .sessionCount(0)
                .averageSessionDuration(0.0)
                .mostActiveHours(List.of(9, 10, 14, 15))
                .preferredContentTypes(List.of("OPPORTUNITY"))
                .totalInteractions(0)
                .engagementLevel("NEW")
                .build();
    }
}