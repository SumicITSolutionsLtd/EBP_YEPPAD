package com.youthconnect.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * User Behavior Data Model for AI Recommendation Processing
 *
 * Captures user engagement patterns, activity metrics, and interaction history
 * Used by recommendation algorithms to personalize content delivery
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorData {

    /**
     * User identifier
     */
    private Long userId;

    /**
     * Total number of distinct sessions user has initiated
     * Higher session count indicates more engaged users
     */
    @Builder.Default
    private int sessionCount = 0;

    /**
     * Average session duration in minutes
     * Indicates quality of engagement - longer sessions suggest deeper interaction
     */
    @Builder.Default
    private double averageSessionDuration = 0.0;

    /**
     * Hours of day when user is most active (0-23)
     * Used for optimal notification timing
     * Example: [9, 10, 14, 15, 20] means user most active at 9am, 10am, 2pm, 3pm, 8pm
     */
    @Builder.Default
    private List<Integer> mostActiveHours = new ArrayList<>();

    /**
     * Content types user engages with most
     * Examples: "OPPORTUNITY", "LEARNING_MODULE", "COMMUNITY_POST", "JOB"
     */
    @Builder.Default
    private List<String> preferredContentTypes = new ArrayList<>();

    /**
     * Total number of interactions (views, clicks, applications) across platform
     * Key metric for measuring overall engagement
     */
    @Builder.Default
    private int totalInteractions = 0;

    /**
     * Number of opportunities user has viewed
     */
    @Builder.Default
    private int opportunityViews = 0;

    /**
     * Number of applications submitted
     */
    @Builder.Default
    private int applicationsSubmitted = 0;

    /**
     * Number of learning modules accessed
     */
    @Builder.Default
    private int learningModulesAccessed = 0;

    /**
     * Number of community posts viewed
     */
    @Builder.Default
    private int communityPostViews = 0;

    /**
     * Application success rate (approved / submitted)
     * Range: 0.0 to 1.0
     */
    @Builder.Default
    private double applicationSuccessRate = 0.0;

    /**
     * Last interaction timestamp (Unix epoch milliseconds)
     */
    private Long lastInteractionTime;

    /**
     * User engagement level classification
     * Values: "NEW", "LOW", "MEDIUM", "HIGH"
     */
    private String engagementLevel;

    /**
     * Device types used (for multi-platform analysis)
     * Examples: "WEB", "MOBILE", "USSD"
     */
    @Builder.Default
    private List<String> deviceTypes = new ArrayList<>();
}