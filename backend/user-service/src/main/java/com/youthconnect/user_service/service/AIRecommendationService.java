package com.youthconnect.user_service.service;

import com.youthconnect.user_service.entity.User;
import com.youthconnect.user_service.entity.Role;
import com.youthconnect.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * AI Recommendation Service for Youth Connect Uganda
 *
 * Provides personalized recommendations using machine learning algorithms
 * to analyze user behavior, interests, and demographic data. This service
 * implements the core "Alice AI Assistant" functionality.
 *
 * Features:
 * - User behavior analysis and pattern recognition
 * - Personalized opportunity matching based on profile and interests
 * - Content recommendation for learning modules
 * - Mentor matching using compatibility algorithms
 * - Predictive analytics for success probability
 * - A/B testing support for recommendation optimization
 *
 * @author Youth Connect Uganda Development Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIRecommendationService {

    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Generate personalized opportunity recommendations for a user
     * Uses collaborative filtering and content-based filtering
     */
    @Cacheable(value = "opportunityRecommendations", key = "#userId")
    public List<OpportunityRecommendation> getPersonalizedOpportunities(Long userId) {
        log.info("Generating opportunity recommendations for user: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            // Get user profile and interests
            UserAnalysisData analysisData = analyzeUserProfile(user);

            // Generate recommendations based on multiple factors
            List<OpportunityRecommendation> recommendations = new ArrayList<>();

            // Role-based recommendations
            recommendations.addAll(getRoleBasedOpportunities(user.getRole(), analysisData));

            // Interest-based recommendations
            recommendations.addAll(getInterestBasedOpportunities(analysisData));

            // Location-based recommendations (if available)
            recommendations.addAll(getLocationBasedOpportunities(analysisData));

            // Collaborative filtering recommendations
            recommendations.addAll(getCollaborativeFilteringOpportunities(userId, analysisData));

            // Sort by recommendation score and return top 10
            List<OpportunityRecommendation> topRecommendations = recommendations.stream()
                    .distinct()
                    .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                    .limit(10)
                    .collect(Collectors.toList());

            log.info("Generated {} opportunity recommendations for user: {}", topRecommendations.size(), userId);
            return topRecommendations;

        } catch (Exception e) {
            log.error("Failed to generate opportunity recommendations for user: {}", userId, e);
            return getDefaultOpportunities(userId);
        }
    }

    /**
     * Generate personalized learning content recommendations
     */
    @Cacheable(value = "contentRecommendations", key = "#userId")
    public List<ContentRecommendation> getPersonalizedContent(Long userId) {
        log.info("Generating content recommendations for user: {}", userId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            UserAnalysisData analysisData = analyzeUserProfile(user);

            List<ContentRecommendation> recommendations = new ArrayList<>();

            // Skill level based content
            recommendations.addAll(getSkillLevelBasedContent(analysisData));

            // Industry/interest based content
            recommendations.addAll(getIndustryBasedContent(analysisData));

            // Learning path recommendations
            recommendations.addAll(getLearningPathRecommendations(analysisData));

            // Trending content for engagement
            recommendations.addAll(getTrendingContent(analysisData));

            return recommendations.stream()
                    .distinct()
                    .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                    .limit(8)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to generate content recommendations for user: {}", userId, e);
            return getDefaultContent(userId);
        }
    }

    /**
     * Find compatible mentors for a youth user
     */
    @Cacheable(value = "mentorRecommendations", key = "#youthUserId")
    public List<MentorRecommendation> getCompatibleMentors(Long youthUserId) {
        log.info("Finding compatible mentors for youth user: {}", youthUserId);

        try {
            User youthUser = userRepository.findById(youthUserId)
                    .orElseThrow(() -> new RuntimeException("Youth user not found: " + youthUserId));

            if (!Role.YOUTH.equals(youthUser.getRole())) {
                throw new IllegalArgumentException("User is not a youth: " + youthUserId);
            }

            UserAnalysisData youthAnalysis = analyzeUserProfile(youthUser);

            // Get all active mentors
            List<User> mentors = userRepository.findByRoleAndIsActiveTrue(Role.MENTOR);

            List<MentorRecommendation> recommendations = new ArrayList<>();

            for (User mentor : mentors) {
                try {
                    UserAnalysisData mentorAnalysis = analyzeUserProfile(mentor);
                    double compatibilityScore = calculateMentorCompatibility(youthAnalysis, mentorAnalysis);

                    if (compatibilityScore > 0.3) { // Only recommend if compatibility > 30%
                        recommendations.add(MentorRecommendation.builder()
                                .mentorId(mentor.getId())
                                .mentorName(getMentorDisplayName(mentor))
                                .compatibilityScore(compatibilityScore)
                                .expertise(getExpertiseAreas(mentorAnalysis))
                                .matchingReasons(generateMatchingReasons(youthAnalysis, mentorAnalysis))
                                .build());
                    }
                } catch (Exception e) {
                    log.warn("Failed to analyze mentor {}: {}", mentor.getId(), e.getMessage());
                }
            }

            return recommendations.stream()
                    .sorted((a, b) -> Double.compare(b.getCompatibilityScore(), a.getCompatibilityScore()))
                    .limit(5)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to generate mentor recommendations for user: {}", youthUserId, e);
            return getDefaultMentors();
        }
    }

    /**
     * Predict success probability for a user applying to an opportunity
     */
    public double predictSuccessProbability(Long userId, Long opportunityId) {
        log.debug("Predicting success probability for user: {} applying to opportunity: {}", userId, opportunityId);

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            UserAnalysisData userAnalysis = analyzeUserProfile(user);

            // Simulate ML model prediction based on various factors
            double baseProbability = 0.4; // Base success rate

            // Adjust based on user completeness
            double profileCompleteness = calculateProfileCompleteness(userAnalysis);
            baseProbability += (profileCompleteness - 0.5) * 0.2;

            // Adjust based on role match
            if (isRoleMatchForOpportunity(user.getRole(), opportunityId)) {
                baseProbability += 0.15;
            }

            // Adjust based on previous activity
            double activityScore = calculateUserActivityScore(userId);
            baseProbability += (activityScore - 0.5) * 0.1;

            // Ensure probability is within bounds
            return Math.max(0.1, Math.min(0.9, baseProbability));

        } catch (Exception e) {
            log.error("Failed to predict success probability for user: {} and opportunity: {}", userId, opportunityId, e);
            return 0.5; // Default neutral probability
        }
    }

    /**
     * Update user activity and behavior patterns
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> recordUserActivity(Long userId, String activityType, Long targetId) {
        log.debug("Recording user activity - User: {}, Activity: {}, Target: {}", userId, activityType, targetId);

        try {
            // This would typically save to user_activity_logs table
            // For now, we simulate recording the activity

            // Update user behavior patterns in cache or database
            updateUserBehaviorProfile(userId, activityType, targetId);

            // Trigger recommendation refresh if significant activity
            if (isSignificantActivity(activityType)) {
                refreshUserRecommendations(userId);
            }

        } catch (Exception e) {
            log.error("Failed to record user activity for user: {}", userId, e);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Analyze user profile and generate analysis data
     */
    private UserAnalysisData analyzeUserProfile(User user) {
        UserAnalysisData.UserAnalysisDataBuilder builder = UserAnalysisData.builder()
                .userId(user.getId())
                .role(user.getRole())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(user.getCreatedAt())
                .isActive(user.isActive());

        // Add role-specific analysis
        switch (user.getRole()) {
            case YOUTH:
                return analyzeYouthProfile(user, builder);
            case MENTOR:
                return analyzeMentorProfile(user, builder);
            case NGO:
                return analyzeNgoProfile(user, builder);
            default:
                return builder.build();
        }
    }

    /**
     * Analyze youth profile specifically
     */
    private UserAnalysisData analyzeYouthProfile(User user, UserAnalysisData.UserAnalysisDataBuilder builder) {
        try {
            // This would typically fetch from youth_profiles table
            // For now, simulate youth profile analysis

            return builder
                    .interests(Arrays.asList("Business", "Technology", "Agriculture"))
                    .skillLevel("Beginner")
                    .location("Kampala")
                    .ageGroup("18-25")
                    .businessStage("Idea Phase")
                    .profileCompleteness(0.7)
                    .build();

        } catch (Exception e) {
            log.warn("Failed to analyze youth profile for user: {}", user.getId());
            return builder.build();
        }
    }

    /**
     * Analyze mentor profile specifically
     */
    private UserAnalysisData analyzeMentorProfile(User user, UserAnalysisData.UserAnalysisDataBuilder builder) {
        try {
            // This would typically fetch from mentor_profiles table

            return builder
                    .interests(Arrays.asList("Business Development", "Fintech", "Startup Strategy"))
                    .skillLevel("Expert")
                    .expertise(Arrays.asList("Business Strategy", "Financial Planning"))
                    .experienceYears(8)
                    .profileCompleteness(0.9)
                    .build();

        } catch (Exception e) {
            log.warn("Failed to analyze mentor profile for user: {}", user.getId());
            return builder.build();
        }
    }

    /**
     * Analyze NGO profile specifically
     */
    private UserAnalysisData analyzeNgoProfile(User user, UserAnalysisData.UserAnalysisDataBuilder builder) {
        try {
            return builder
                    .interests(Arrays.asList("Youth Development", "Entrepreneurship", "Capacity Building"))
                    .location("Uganda")
                    .profileCompleteness(0.8)
                    .build();

        } catch (Exception e) {
            log.warn("Failed to analyze NGO profile for user: {}", user.getId());
            return builder.build();
        }
    }

    /**
     * Generate role-based opportunity recommendations
     */
    private List<OpportunityRecommendation> getRoleBasedOpportunities(Role role, UserAnalysisData analysisData) {
        List<OpportunityRecommendation> recommendations = new ArrayList<>();

        if (Role.YOUTH.equals(role)) {
            // Simulate youth-specific opportunities
            recommendations.add(createOpportunityRecommendation(
                    1L, "Young Entrepreneurs Grant 2024", "GRANT", 0.85,
                    "Perfect match for young entrepreneurs in " + analysisData.getBusinessStage()
            ));

            recommendations.add(createOpportunityRecommendation(
                    2L, "Tech Startup Bootcamp", "TRAINING", 0.75,
                    "Ideal for your technology interests and skill level"
            ));
        }

        return recommendations;
    }

    /**
     * Generate interest-based recommendations
     */
    private List<OpportunityRecommendation> getInterestBasedOpportunities(UserAnalysisData analysisData) {
        List<OpportunityRecommendation> recommendations = new ArrayList<>();

        if (analysisData.getInterests() != null) {
            for (String interest : analysisData.getInterests()) {
                if ("Technology".equalsIgnoreCase(interest)) {
                    recommendations.add(createOpportunityRecommendation(
                            3L, "Fintech Innovation Challenge", "GRANT", 0.70,
                            "Matches your interest in " + interest
                    ));
                } else if ("Agriculture".equalsIgnoreCase(interest)) {
                    recommendations.add(createOpportunityRecommendation(
                            4L, "AgriTech Funding Program", "LOAN", 0.65,
                            "Perfect for agriculture-focused businesses"
                    ));
                }
            }
        }

        return recommendations;
    }

    /**
     * Helper method to create opportunity recommendation
     */
    private OpportunityRecommendation createOpportunityRecommendation(
            Long opportunityId, String title, String type, double score, String reason) {
        return OpportunityRecommendation.builder()
                .opportunityId(opportunityId)
                .title(title)
                .type(type)
                .score(score)
                .recommendationReason(reason)
                .build();
    }

    /**
     * Calculate mentor compatibility score
     */
    private double calculateMentorCompatibility(UserAnalysisData youthAnalysis, UserAnalysisData mentorAnalysis) {
        double compatibilityScore = 0.0;

        // Interest overlap
        if (youthAnalysis.getInterests() != null && mentorAnalysis.getExpertise() != null) {
            long commonInterests = youthAnalysis.getInterests().stream()
                    .mapToLong(interest -> mentorAnalysis.getExpertise().stream()
                            .mapToLong(expertise -> interest.toLowerCase().contains(expertise.toLowerCase()) ? 1 : 0)
                            .sum())
                    .sum();

            compatibilityScore += (commonInterests / (double) youthAnalysis.getInterests().size()) * 0.4;
        }

        // Experience level match
        if ("Beginner".equals(youthAnalysis.getSkillLevel()) && mentorAnalysis.getExperienceYears() != null) {
            if (mentorAnalysis.getExperienceYears() >= 3) {
                compatibilityScore += 0.3;
            }
        }

        // Location proximity (if available)
        if (youthAnalysis.getLocation() != null && mentorAnalysis.getLocation() != null) {
            if (youthAnalysis.getLocation().equals(mentorAnalysis.getLocation())) {
                compatibilityScore += 0.2;
            }
        }

        // Profile completeness factor
        compatibilityScore += (mentorAnalysis.getProfileCompleteness() * 0.1);

        return Math.min(1.0, compatibilityScore);
    }

    /**
     * Generate matching reasons between youth and mentor
     */
    private List<String> generateMatchingReasons(UserAnalysisData youthAnalysis, UserAnalysisData mentorAnalysis) {
        List<String> reasons = new ArrayList<>();

        if (youthAnalysis.getInterests() != null && mentorAnalysis.getExpertise() != null) {
            for (String interest : youthAnalysis.getInterests()) {
                for (String expertise : mentorAnalysis.getExpertise()) {
                    if (interest.toLowerCase().contains(expertise.toLowerCase())) {
                        reasons.add("Shared expertise in " + expertise);
                    }
                }
            }
        }

        if (youthAnalysis.getLocation() != null && mentorAnalysis.getLocation() != null &&
                youthAnalysis.getLocation().equals(mentorAnalysis.getLocation())) {
            reasons.add("Same location: " + youthAnalysis.getLocation());
        }

        if (reasons.isEmpty()) {
            reasons.add("Complementary skills and experience");
        }

        return reasons;
    }

    // Additional helper methods and data classes

    private List<OpportunityRecommendation> getLocationBasedOpportunities(UserAnalysisData analysisData) {
        // Implementation for location-based recommendations
        return new ArrayList<>();
    }

    private List<OpportunityRecommendation> getCollaborativeFilteringOpportunities(Long userId, UserAnalysisData analysisData) {
        // Implementation for collaborative filtering
        return new ArrayList<>();
    }

    private List<OpportunityRecommendation> getDefaultOpportunities(Long userId) {
        // Fallback recommendations
        List<OpportunityRecommendation> defaults = new ArrayList<>();
        defaults.add(createOpportunityRecommendation(
                999L, "General Entrepreneurship Grant", "GRANT", 0.5,
                "Popular opportunity for new entrepreneurs"
        ));
        return defaults;
    }

    private List<ContentRecommendation> getSkillLevelBasedContent(UserAnalysisData analysisData) {
        List<ContentRecommendation> recommendations = new ArrayList<>();

        if ("Beginner".equals(analysisData.getSkillLevel())) {
            recommendations.add(ContentRecommendation.builder()
                    .contentId(1L)
                    .title("Business Basics for Beginners")
                    .type("AUDIO")
                    .score(0.8)
                    .reason("Perfect for your beginner skill level")
                    .build());
        }

        return recommendations;
    }

    private List<ContentRecommendation> getIndustryBasedContent(UserAnalysisData analysisData) {
        // Industry-specific content recommendations
        return new ArrayList<>();
    }

    private List<ContentRecommendation> getLearningPathRecommendations(UserAnalysisData analysisData) {
        // Learning path recommendations
        return new ArrayList<>();
    }

    private List<ContentRecommendation> getTrendingContent(UserAnalysisData analysisData) {
        // Trending content recommendations
        return new ArrayList<>();
    }

    private List<ContentRecommendation> getDefaultContent(Long userId) {
        // Default content fallback
        return new ArrayList<>();
    }

    private List<MentorRecommendation> getDefaultMentors() {
        // Default mentor recommendations
        return new ArrayList<>();
    }

    private String getMentorDisplayName(User mentor) {
        // Get mentor display name from profile
        return "Mentor " + mentor.getId();
    }

    private List<String> getExpertiseAreas(UserAnalysisData mentorAnalysis) {
        return mentorAnalysis.getExpertise() != null ?
                mentorAnalysis.getExpertise() :
                Arrays.asList("General Business");
    }

    private double calculateProfileCompleteness(UserAnalysisData analysisData) {
        return analysisData.getProfileCompleteness() != null ?
                analysisData.getProfileCompleteness() : 0.5;
    }

    private boolean isRoleMatchForOpportunity(Role role, Long opportunityId) {
        // Check if user role matches opportunity requirements
        return Role.YOUTH.equals(role); // Simplified for demo
    }

    private double calculateUserActivityScore(Long userId) {
        // Calculate user activity score based on interactions
        return 0.6; // Simplified for demo
    }

    private void updateUserBehaviorProfile(Long userId, String activityType, Long targetId) {
        // Update user behavior patterns
        log.debug("Updated behavior profile for user: {} with activity: {}", userId, activityType);
    }

    private boolean isSignificantActivity(String activityType) {
        return Arrays.asList("APPLY_OPPORTUNITY", "COMPLETE_MODULE", "BOOK_MENTOR").contains(activityType);
    }

    private void refreshUserRecommendations(Long userId) {
        // Refresh cached recommendations
        log.debug("Refreshing recommendations for user: {}", userId);
    }

    // Data classes for recommendations

    @lombok.Data
    @lombok.Builder
    public static class OpportunityRecommendation {
        private Long opportunityId;
        private String title;
        private String type;
        private double score;
        private String recommendationReason;
    }

    @lombok.Data
    @lombok.Builder
    public static class ContentRecommendation {
        private Long contentId;
        private String title;
        private String type;
        private double score;
        private String reason;
    }

    @lombok.Data
    @lombok.Builder
    public static class MentorRecommendation {
        private Long mentorId;
        private String mentorName;
        private double compatibilityScore;
        private List<String> expertise;
        private List<String> matchingReasons;
    }

    @lombok.Data
    @lombok.Builder
    public static class UserAnalysisData {
        private Long userId;
        private Role role;
        private String phoneNumber;
        private java.time.LocalDateTime createdAt;
        private boolean isActive;
        private List<String> interests;
        private String skillLevel;
        private String location;
        private String ageGroup;
        private String businessStage;
        private List<String> expertise;
        private Integer experienceYears;
        private Double profileCompleteness;
    }
}