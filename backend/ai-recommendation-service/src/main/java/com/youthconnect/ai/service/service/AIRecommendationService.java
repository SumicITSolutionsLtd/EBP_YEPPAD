package com.youthconnect.ai.service.service;

import com.youthconnect.ai_service.client.UserServiceClient;
import com.youthconnect.ai_service.dto.*;
import com.youthconnect.ai_service.model.UserBehaviorData;
import com.youthconnect.ai_service.model.UserProfileData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIRecommendationService {

    private final UserServiceClient userServiceClient;
    private final RecommendationAlgorithmService algorithmService;
    private final MLModelService mlModelService;

    /**
     * Generate personalized opportunity recommendations using AI algorithms
     */
    @Cacheable(value = "opportunityRecommendations", key = "#userId + '_' + #limit")
    public List<OpportunityRecommendation> getPersonalizedOpportunities(Long userId, int limit) {
        log.info("Generating personalized opportunities for user: {}", userId);

        try {
            // Get user profile and behavior data
            UserProfileData userProfile = getUserProfileFromService(userId);
            UserBehaviorData behaviorData = getUserBehaviorData(userId);

            // Get available opportunities (simulated for now)
            List<OpportunityData> opportunities = getAvailableOpportunities();

            List<OpportunityRecommendation> recommendations = new ArrayList<>();

            for (OpportunityData opportunity : opportunities) {
                // Calculate recommendation score using multiple algorithms
                double score = algorithmService.calculateOpportunityScore(userProfile, behaviorData, opportunity);

                if (score > 0.3) { // Only recommend if score > 30%
                    recommendations.add(OpportunityRecommendation.builder()
                            .opportunityId(opportunity.getId())
                            .title(opportunity.getTitle())
                            .type(opportunity.getType())
                            .score(score)
                            .reason(generateOpportunityReason(userProfile, opportunity, score))
                            .tags(opportunity.getTags())
                            .deadline(opportunity.getDeadline())
                            .fundingAmount(opportunity.getFundingAmount())
                            .build());
                }
            }

            // Sort by score and apply limit
            List<OpportunityRecommendation> topRecommendations = recommendations.stream()
                    .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                    .limit(limit)
                    .collect(Collectors.toList());

            log.info("Generated {} opportunity recommendations for user: {}", topRecommendations.size(), userId);
            return topRecommendations;

        } catch (Exception e) {
            log.error("Error generating opportunity recommendations for user {}: {}", userId, e.getMessage());
            return getDefaultOpportunityRecommendations(userId, limit);
        }
    }

    /**
     * Generate personalized learning content recommendations
     */
    @Cacheable(value = "contentRecommendations", key = "#userId + '_' + #limit")
    public List<ContentRecommendation> getPersonalizedContent(Long userId, int limit) {
        log.info("Generating personalized content for user: {}", userId);

        try {
            UserProfileData userProfile = getUserProfileFromService(userId);
            List<ContentData> allContent = getAvailableLearningContent();

            List<ContentRecommendation> recommendations = new ArrayList<>();

            for (ContentData content : allContent) {
                double score = algorithmService.calculateContentScore(userProfile, content);

                if (score > 0.4) {
                    recommendations.add(ContentRecommendation.builder()
                            .contentId(content.getId())
                            .title(content.getTitle())
                            .type(content.getType())
                            .score(score)
                            .reason(generateContentReason(userProfile, content))
                            .language(content.getLanguage())
                            .duration(content.getDuration())
                            .difficulty(content.getDifficulty())
                            .build());
                }
            }

            return recommendations.stream()
                    .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error generating content recommendations for user {}: {}", userId, e.getMessage());
            return getDefaultContentRecommendations(userId, limit);
        }
    }

    /**
     * Find compatible mentors using AI matching algorithms
     */
    @Cacheable(value = "mentorRecommendations", key = "#youthUserId + '_' + #limit")
    public List<MentorRecommendation> getCompatibleMentors(Long youthUserId, int limit) {
        log.info("Finding compatible mentors for youth user: {}", youthUserId);

        try {
            UserProfileData youthProfile = getUserProfileFromService(youthUserId);
            List<MentorData> availableMentors = getAvailableMentors();

            if (!"YOUTH".equals(youthProfile.getRole())) {
                throw new IllegalArgumentException("User is not a youth: " + youthUserId);
            }

            List<MentorRecommendation> recommendations = new ArrayList<>();

            for (MentorData mentor : availableMentors) {
                double compatibility = algorithmService.calculateMentorCompatibility(youthProfile, mentor);

                if (compatibility > 0.3) {
                    recommendations.add(MentorRecommendation.builder()
                            .mentorId(mentor.getId())
                            .mentorName(mentor.getFirstName() + " " + mentor.getLastName())
                            .compatibilityScore(compatibility)
                            .expertise(mentor.getExpertiseAreas())
                            .experience(mentor.getExperienceYears())
                            .matchingReasons(generateMentorMatchingReasons(youthProfile, mentor))
                            .location(mentor.getLocation())
                            .availabilityStatus(mentor.getAvailabilityStatus())
                            .build());
                }
            }

            return recommendations.stream()
                    .sorted((a, b) -> Double.compare(b.getCompatibilityScore(), a.getCompatibilityScore()))
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error generating mentor recommendations for user {}: {}", youthUserId, e.getMessage());
            return getDefaultMentorRecommendations(youthUserId, limit);
        }
    }

    /**
     * Predict success probability using ML models
     */
    public double predictSuccessProbability(Long userId, Long opportunityId) {
        log.debug("Predicting success probability for user {} and opportunity {}", userId, opportunityId);

        try {
            UserProfileData userProfile = getUserProfileFromService(userId);
            OpportunityData opportunity = getOpportunityById(opportunityId);
            UserBehaviorData behaviorData = getUserBehaviorData(userId);

            // Use ML model for prediction
            return mlModelService.predictSuccessProbability(userProfile, opportunity, behaviorData);

        } catch (Exception e) {
            log.error("Error predicting success probability: {}", e.getMessage());
            // Return default probability based on simple heuristics
            return calculateBasicSuccessProbability(userId, opportunityId);
        }
    }

    /**
     * Get user behavior insights and patterns
     */
    public UserBehaviorInsights getUserBehaviorInsights(Long userId) {
        try {
            UserBehaviorData behaviorData = getUserBehaviorData(userId);
            UserProfileData profileData = getUserProfileFromService(userId);

            return UserBehaviorInsights.builder()
                    .userId(userId)
                    .mostActiveHours(behaviorData.getMostActiveHours())
                    .preferredContent(behaviorData.getPreferredContentTypes())
                    .engagementLevel(algorithmService.calculateEngagementLevel(behaviorData))
                    .topInterests(algorithmService.extractTopInterests(behaviorData))
                    .applicationSuccessRate(algorithmService.calculateApplicationSuccessRate(userId))
                    .recommendedActions(algorithmService.generateRecommendedActions(profileData, behaviorData))
                    .build();

        } catch (Exception e) {
            log.error("Error getting behavior insights for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get behavior insights", e);
        }
    }

    // Service health checks
    public boolean checkMLModelsHealth() {
        return mlModelService.isHealthy();
    }

    public boolean checkDataServiceHealth() {
        try {
            // Test connection to user service
            userServiceClient.checkHealth();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Private helper methods
    private UserProfileData getUserProfileFromService(Long userId) {
        // This would call user service to get profile data
        // For now, return simulated data
        return UserProfileData.builder()
                .userId(userId)
                .role("YOUTH")
                .interests(Arrays.asList("Business", "Technology", "Agriculture"))
                .skillLevel("Beginner")
                .location("Kampala")
                .ageGroup("18-25")
                .businessStage("Idea Phase")
                .profileCompleteness(0.7)
                .build();
    }

    private UserBehaviorData getUserBehaviorData(Long userId) {
        // Simulate user behavior data
        return UserBehaviorData.builder()
                .userId(userId)
                .sessionCount(45)
                .averageSessionDuration(12.5)
                .mostActiveHours(Arrays.asList(9, 10, 14, 15, 20))
                .preferredContentTypes(Arrays.asList("AUDIO", "VIDEO"))
                .totalInteractions(156)
                .build();
    }

    // Simulated data methods - in production, these would call actual services
    private List<OpportunityData> getAvailableOpportunities() {
        return Arrays.asList(
                OpportunityData.builder()
                        .id(1L)
                        .title("Young Entrepreneurs Grant 2024")
                        .type("GRANT")
                        .tags(Arrays.asList("Business", "Startup", "Youth"))
                        .fundingAmount(5000000L) // 5M UGX
                        .build(),
                OpportunityData.builder()
                        .id(2L)
                        .title("Tech Innovation Training")
                        .type("TRAINING")
                        .tags(Arrays.asList("Technology", "Innovation", "Skills"))
                        .build()
        );
    }

    private List<ContentData> getAvailableLearningContent() {
        return Arrays.asList(
                ContentData.builder()
                        .id(1L)
                        .title("Business Planning Fundamentals")
                        .type("AUDIO")
                        .language("English")
                        .difficulty("Beginner")
                        .duration(25)
                        .tags(Arrays.asList("Business", "Planning"))
                        .build(),
                ContentData.builder()
                        .id(2L)
                        .title("Financial Management for Startups")
                        .type("VIDEO")
                        .language("Luganda")
                        .difficulty("Intermediate")
                        .duration(40)
                        .tags(Arrays.asList("Finance", "Startup"))
                        .build()
        );
    }

    private List<MentorData> getAvailableMentors() {
        return Arrays.asList(
                MentorData.builder()
                        .id(100L)
                        .firstName("Sarah")
                        .lastName("Nakato")
                        .expertiseAreas(Arrays.asList("Business Development", "Marketing"))
                        .experienceYears(8)
                        .location("Kampala")
                        .availabilityStatus("AVAILABLE")
                        .build()
        );
    }

    private OpportunityData getOpportunityById(Long id) {
        return getAvailableOpportunities().stream()
                .filter(opp -> opp.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private double calculateBasicSuccessProbability(Long userId, Long opportunityId) {
        // Simple heuristic-based probability
        return 0.6; // Default 60% success probability
    }

    // Default/fallback methods
    private List<OpportunityRecommendation> getDefaultOpportunityRecommendations(Long userId, int limit) {
        return Arrays.asList(
                OpportunityRecommendation.builder()
                        .opportunityId(999L)
                        .title("General Entrepreneurship Grant")
                        .type("GRANT")
                        .score(0.5)
                        .reason("Popular opportunity for new entrepreneurs")
                        .build()
        );
    }

    private List<ContentRecommendation> getDefaultContentRecommendations(Long userId, int limit) {
        return Arrays.asList(
                ContentRecommendation.builder()
                        .contentId(1L)
                        .title("Business Basics for Beginners")
                        .type("AUDIO")
                        .score(0.6)
                        .reason("Essential content for new entrepreneurs")
                        .build()
        );
    }

    private List<MentorRecommendation> getDefaultMentorRecommendations(Long youthUserId, int limit) {
        return Arrays.asList();
    }

    // Helper methods for generating reasons
    private String generateOpportunityReason(UserProfileData user, OpportunityData opportunity, double score) {
        if (score > 0.8) {
            return "Excellent match based on your interests in " + String.join(", ", user.getInterests());
        } else if (score > 0.6) {
            return "Good fit for your " + user.getSkillLevel() + " skill level";
        } else {
            return "Relevant to your business stage: " + user.getBusinessStage();
        }
    }

    private String generateContentReason(UserProfileData user, ContentData content) {
        return "Recommended based on your interests and " + user.getSkillLevel() + " skill level";
    }

    private List<String> generateMentorMatchingReasons(UserProfileData youth, MentorData mentor) {
        List<String> reasons = new ArrayList<>();
        reasons.add("Expertise in " + String.join(", ", mentor.getExpertiseAreas()));
        if (mentor.getLocation().equals(youth.getLocation())) {
            reasons.add("Same location: " + mentor.getLocation());
        }
        return reasons;
    }
}
