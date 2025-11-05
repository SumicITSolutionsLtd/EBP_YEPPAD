package com.youthconnect.ai.service.service.impl;

import com.youthconnect.ai.service.client.JobServiceClient;
import com.youthconnect.ai.service.client.UserServiceClient;
import com.youthconnect.ai.service.dto.*;
import com.youthconnect.ai.service.model.*;
import com.youthconnect.ai.service.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Recommendation Service Implementation
 * Provides intelligent recommendations using machine learning algorithms
 *
 * Features:
 * - Collaborative filtering (user-item interactions)
 * - Content-based filtering (profile-opportunity matching)
 * - Hybrid approach (combines multiple algorithms)
 * - Real-time personalization
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIRecommendationServiceImpl implements AIRecommendationService {

    private final JobServiceClient jobServiceClient;
    private final UserServiceClient userServiceClient;
    private final RecommendationAlgorithmService algorithmService;
    private final MLModelService mlModelService;
    private final UserActivityService activityService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = "opportunityRecommendations", key = "#userId + '_' + #limit")
    public List<OpportunityRecommendation> getPersonalizedOpportunities(Long userId, int limit) {
        log.info("Generating personalized opportunities for user: {}", userId);

        try {
            // Fetch user profile and behavior data
            UserProfileData userProfile = getUserProfileFromService(userId);
            UserBehaviorData behaviorData = activityService.getUserBehaviorData(userId, 30);

            // Get available opportunities (simulated for now)
            List<OpportunityData> opportunities = getAvailableOpportunities();

            List<OpportunityRecommendation> recommendations = new ArrayList<>();

            // Score each opportunity
            for (OpportunityData opportunity : opportunities) {
                double score = algorithmService.calculateOpportunityScore(
                        userProfile,
                        behaviorData,
                        opportunity
                );

                if (score > 0.3) { // Only recommend if score > 30%
                    recommendations.add(OpportunityRecommendation.builder()
                            .opportunityId(opportunity.getId())
                            .title(opportunity.getTitle())
                            .type(opportunity.getType())
                            .matchScore(score)
                            .reason(generateOpportunityReason(userProfile, opportunity, score))
                            .tags(opportunity.getTags())
                            .deadline(opportunity.getDeadline())
                            .fundingAmount(opportunity.getFundingAmount())
                            .build());
                }
            }

            // Sort by score and apply limit
            List<OpportunityRecommendation> topRecommendations = recommendations.stream()
                    .sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()))
                    .limit(limit)
                    .collect(Collectors.toList());

            log.info("Generated {} opportunity recommendations for user: {}",
                    topRecommendations.size(), userId);

            return topRecommendations;

        } catch (Exception e) {
            log.error("Error generating opportunity recommendations for user {}: {}",
                    userId, e.getMessage());
            return getDefaultOpportunityRecommendations(limit);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
            log.error("Error generating content recommendations for user {}: {}",
                    userId, e.getMessage());
            return getDefaultContentRecommendations(limit);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
                double compatibility = algorithmService.calculateMentorCompatibility(
                        youthProfile,
                        mentor
                );

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
                    .sorted((a, b) -> Double.compare(
                            b.getCompatibilityScore(),
                            a.getCompatibilityScore()
                    ))
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error generating mentor recommendations for user {}: {}",
                    youthUserId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double predictSuccessProbability(Long userId, Long opportunityId) {
        log.debug("Predicting success probability for user {} and opportunity {}",
                userId, opportunityId);

        try {
            UserProfileData userProfile = getUserProfileFromService(userId);
            OpportunityData opportunity = getOpportunityById(opportunityId);
            UserBehaviorData behaviorData = activityService.getUserBehaviorData(userId, 30);

            // Use ML model for prediction
            return mlModelService.predictSuccessProbability(
                    userProfile,
                    opportunity,
                    behaviorData
            );

        } catch (Exception e) {
            log.error("Error predicting success probability: {}", e.getMessage());
            return calculateBasicSuccessProbability(userId, opportunityId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserBehaviorInsights getUserBehaviorInsights(Long userId) {
        try {
            Map<String, Object> analysisData = activityService.getUserBehaviorAnalysis(userId, 30);

            return UserBehaviorInsights.builder()
                    .userId(userId)
                    .mostActiveHours((List<Integer>) analysisData.get("mostActiveHours"))
                    .preferredContent((List<String>) analysisData.get("preferredContentTypes"))
                    .engagementLevel((String) analysisData.get("engagementLevel"))
                    .topInterests((List<String>) analysisData.get("topInterests"))
                    .applicationSuccessRate(algorithmService.calculateApplicationSuccessRate(userId))
                    .recommendedActions(algorithmService.generateRecommendedActions(
                            getUserProfileFromService(userId),
                            null
                    ))
                    .build();

        } catch (Exception e) {
            log.error("Error getting behavior insights for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to get behavior insights", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkMLModelsHealth() {
        return mlModelService.isHealthy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkDataServiceHealth() {
        try {
            userServiceClient.checkHealth();
            jobServiceClient.getRecentJobs(1);
            return true;
        } catch (Exception e) {
            log.error("Data service health check failed: {}", e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================

    /**
     * Fetch user profile from user-service
     * In production, this calls the actual user service via Feign
     */
    private UserProfileData getUserProfileFromService(Long userId) {
        // TODO: Call user-service to get actual profile
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

    /**
     * Get available opportunities
     * TODO: Replace with call to opportunity-service
     */
    private List<OpportunityData> getAvailableOpportunities() {
        return Arrays.asList(
                OpportunityData.builder()
                        .id(1L)
                        .title("Young Entrepreneurs Grant 2025")
                        .type("GRANT")
                        .tags(Arrays.asList("Business", "Startup", "Youth"))
                        .location("Kampala")
                        .deadline(LocalDateTime.now().plusDays(30))
                        .fundingAmount(5000000L) // 5M UGX
                        .build(),
                OpportunityData.builder()
                        .id(2L)
                        .title("Tech Innovation Training")
                        .type("TRAINING")
                        .tags(Arrays.asList("Technology", "Innovation", "Skills"))
                        .location("Nebbi")
                        .deadline(LocalDateTime.now().plusDays(15))
                        .build()
        );
    }

    /**
     * Get available learning content
     * TODO: Replace with call to content-service
     */
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

    /**
     * Get available mentors
     * TODO: Replace with call to mentor-service
     */
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
        return 0.6; // Default 60% success probability
    }

    private List<OpportunityRecommendation> getDefaultOpportunityRecommendations(int limit) {
        return Arrays.asList(
                OpportunityRecommendation.builder()
                        .opportunityId(999L)
                        .title("General Entrepreneurship Grant")
                        .type("GRANT")
                        .matchScore(0.5)
                        .reason("Popular opportunity for new entrepreneurs")
                        .build()
        );
    }

    private List<ContentRecommendation> getDefaultContentRecommendations(int limit) {
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

    // Reason generation methods

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