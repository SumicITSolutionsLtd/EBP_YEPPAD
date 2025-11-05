package com.youthconnect.ai.service.service;

import com.youthconnect.ai.service.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Machine Learning Model Service
 *
 * Handles ML model inference and predictions for recommendation system
 * Current implementation uses heuristic-based algorithms (STUB)
 *
 * Future Enhancement: Integrate Apache Mahout or TensorFlow for collaborative filtering
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MLModelService {

    /**
     * Predict success probability using ML model
     *
     * Current Algorithm: Heuristic-based scoring
     * - Profile completeness: 20% weight
     * - User engagement: 15% weight
     * - Experience level: 15% weight
     * - Interest alignment: 30% weight
     * - Location proximity: 10% weight
     * - Recent activity: 10% weight
     *
     * @param userProfile User profile data
     * @param opportunity Opportunity details
     * @param behaviorData User behavior metrics
     * @return Success probability (0.0 - 1.0)
     */
    public double predictSuccessProbability(
            UserProfileData userProfile,
            OpportunityData opportunity,
            UserBehaviorData behaviorData) {

        log.debug("Predicting success probability for user: {} and opportunity: {}",
                userProfile.getUserId(), opportunity.getId());

        double probability = 0.5; // Base probability 50%

        // Factor 1: Profile completeness (0-20% contribution)
        probability += userProfile.getProfileCompleteness() * 0.2;

        // Factor 2: User engagement (0-15% contribution)
        if (behaviorData.getSessionCount() > 10) {
            probability += 0.10; // Engaged user bonus
        }
        if (behaviorData.getApplicationSuccessRate() > 0.5) {
            probability += 0.05; // Historical success bonus
        }

        // Factor 3: Experience/skill level match (0-15% contribution)
        String userSkill = userProfile.getSkillLevel();
        String oppLevel = opportunity.getExperienceLevel();
        if (userSkill != null && oppLevel != null) {
            if (userSkill.equalsIgnoreCase(oppLevel)) {
                probability += 0.15; // Perfect match
            } else if (isSkillLevelCompatible(userSkill, oppLevel)) {
                probability += 0.08; // Acceptable match
            }
        }

        // Factor 4: Interest alignment (0-30% contribution)
        long matchingInterests = userProfile.getInterests().stream()
                .filter(interest -> opportunity.getTags().contains(interest))
                .count();

        double interestScore = matchingInterests > 0
                ? (double) matchingInterests / Math.max(userProfile.getInterests().size(), 1)
                : 0.0;
        probability += interestScore * 0.3;

        // Factor 5: Location proximity (0-10% contribution)
        if (userProfile.getLocation() != null &&
                userProfile.getLocation().equalsIgnoreCase(opportunity.getLocation())) {
            probability += 0.10;
        }

        // Factor 6: Recent activity (0-10% contribution)
        if (behaviorData.getTotalInteractions() > 50) {
            probability += 0.05;
        }
        if (behaviorData.getEngagementLevel() != null &&
                behaviorData.getEngagementLevel().equals("HIGH")) {
            probability += 0.05;
        }

        // Cap at 95% (never 100% certainty)
        return Math.min(probability, 0.95);
    }

    /**
     * Check if skill levels are compatible
     *
     * @param userSkill User's skill level
     * @param requiredLevel Required skill level
     * @return true if compatible
     */
    private boolean isSkillLevelCompatible(String userSkill, String requiredLevel) {
        if (userSkill == null || requiredLevel == null) return false;

        // Beginner can apply to Beginner
        if (userSkill.equalsIgnoreCase("Beginner") &&
                requiredLevel.equalsIgnoreCase("Beginner")) {
            return true;
        }

        // Intermediate can apply to Beginner and Intermediate
        if (userSkill.equalsIgnoreCase("Intermediate") &&
                (requiredLevel.equalsIgnoreCase("Beginner") ||
                        requiredLevel.equalsIgnoreCase("Intermediate"))) {
            return true;
        }

        // Advanced can apply to all levels
        if (userSkill.equalsIgnoreCase("Advanced")) {
            return true;
        }

        return false;
    }

    /**
     * Check if ML models are healthy and loaded
     *
     * @return true if models operational (always true for heuristic implementation)
     */
    public boolean isHealthy() {
        try {
            // STUB: In production, this would check:
            // - Model files exist and are loadable
            // - Model versions are current
            // - Prediction endpoints are responsive
            // - Training data is fresh

            log.debug("ML Model health check: OK (heuristic implementation)");
            return true;

        } catch (Exception e) {
            log.error("ML Model health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Calculate recommendation confidence score
     * Used to determine if recommendation should be shown to user
     *
     * @param matchScore Algorithm-generated match score
     * @param behaviorData User behavior data
     * @return Confidence level (0.0 - 1.0)
     */
    public double calculateConfidence(double matchScore, UserBehaviorData behaviorData) {
        double confidence = matchScore;

        // Boost confidence for engaged users (they're more predictable)
        if (behaviorData.getSessionCount() > 20) {
            confidence += 0.05;
        }

        // Reduce confidence for new users (less data to work with)
        if (behaviorData.getSessionCount() < 5) {
            confidence *= 0.8;
        }

        return Math.min(confidence, 1.0);
    }
}