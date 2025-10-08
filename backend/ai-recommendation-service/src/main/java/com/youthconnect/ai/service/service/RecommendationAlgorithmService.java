package com.youthconnect.ai.service.service;

import com.youthconnect.ai_service.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * STUB Implementation: Recommendation Algorithm Service
 * Contains the core recommendation algorithms for the AI system
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationAlgorithmService {

    /**
     * Calculate opportunity recommendation score using collaborative filtering
     */
    public double calculateOpportunityScore(UserProfileData userProfile,
                                            UserBehaviorData behaviorData,
                                            OpportunityData opportunity) {
        log.debug("Calculating opportunity score for user: {} and opportunity: {}",
                userProfile.getUserId(), opportunity.getId());

        // STUB: Simple algorithm based on interest matching
        double score = 0.0;

        // Interest matching (40% weight)
        long matchingInterests = userProfile.getInterests().stream()
                .filter(interest -> opportunity.getTags().contains(interest))
                .count();
        score += (matchingInterests * 0.4) / Math.max(userProfile.getInterests().size(), 1);

        // Skill level matching (30% weight)
        if ("Beginner".equals(userProfile.getSkillLevel()) &&
                opportunity.getTags().contains("Beginner")) {
            score += 0.3;
        }

        // Location preference (20% weight)
        if (userProfile.getLocation().equals(opportunity.getLocation())) {
            score += 0.2;
        }

        // User engagement factor (10% weight)
        score += Math.min(behaviorData.getSessionCount() / 100.0, 0.1);

        return Math.min(score, 1.0);
    }

    /**
     * Calculate content recommendation score
     */
    public double calculateContentScore(UserProfileData userProfile, ContentData content) {
        log.debug("Calculating content score for user: {}", userProfile.getUserId());

        // STUB: Simple content matching algorithm
        double score = 0.5; // Base score

        // Interest matching
        long matchingTags = userProfile.getInterests().stream()
                .filter(interest -> content.getTags().contains(interest))
                .count();

        score += (matchingTags * 0.3) / Math.max(userProfile.getInterests().size(), 1);

        // Skill level appropriateness
        if (userProfile.getSkillLevel().equals(content.getDifficulty())) {
            score += 0.2;
        }

        return Math.min(score, 1.0);
    }

    /**
     * Calculate mentor compatibility score
     */
    public double calculateMentorCompatibility(UserProfileData youthProfile, MentorData mentor) {
        log.debug("Calculating mentor compatibility for youth: {}", youthProfile.getUserId());

        double compatibility = 0.0;

        // Expertise matching with youth interests
        long matchingExpertise = youthProfile.getInterests().stream()
                .filter(interest -> mentor.getExpertiseAreas().contains(interest))
                .count();

        compatibility += (matchingExpertise * 0.5) / Math.max(youthProfile.getInterests().size(), 1);

        // Location proximity
        if (youthProfile.getLocation().equals(mentor.getLocation())) {
            compatibility += 0.3;
        }

        // Experience factor
        if (mentor.getExperienceYears() >= 5) {
            compatibility += 0.2;
        }

        return Math.min(compatibility, 1.0);
    }

    /**
     * Calculate user engagement level
     */
    public String calculateEngagementLevel(UserBehaviorData behaviorData) {
        double sessions = behaviorData.getSessionCount();
        double avgDuration = behaviorData.getAverageSessionDuration();

        if (sessions >= 20 && avgDuration >= 15) return "HIGH";
        if (sessions >= 10 && avgDuration >= 10) return "MEDIUM";
        if (sessions >= 5) return "LOW";
        return "NEW";
    }

    /**
     * Extract top interests from behavior data
     */
    public List<String> extractTopInterests(UserBehaviorData behaviorData) {
        // STUB: Return default interests based on activity
        return List.of("Business", "Technology", "Agriculture", "Finance");
    }

    /**
     * Calculate application success rate for user
     */
    public double calculateApplicationSuccessRate(Long userId) {
        // STUB: Return mock success rate
        return 0.65; // 65% success rate
    }

    /**
     * Generate recommended actions for user
     */
    public List<String> generateRecommendedActions(UserProfileData profile, UserBehaviorData behavior) {
        // STUB: Basic recommendations
        return List.of(
                "Complete your profile to get better recommendations",
                "Apply to at least 2 opportunities this month",
                "Attend a mentorship session",
                "Complete a business skills learning module"
        );
    }
}