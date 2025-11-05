package com.youthconnect.ai.service.service;

import com.youthconnect.ai.service.dto.*;

import java.util.List;

/**
 * AI Recommendation Service Interface
 * Defines contract for intelligent recommendation generation
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
public interface AIRecommendationService {

    /**
     * Generate personalized opportunity recommendations for a user
     *
     * @param userId User ID
     * @param limit Maximum number of recommendations to return
     * @return List of ranked opportunity recommendations
     */
    List<OpportunityRecommendation> getPersonalizedOpportunities(Long userId, int limit);

    /**
     * Generate personalized learning content recommendations
     *
     * @param userId User ID
     * @param limit Maximum number of recommendations
     * @return List of ranked content recommendations
     */
    List<ContentRecommendation> getPersonalizedContent(Long userId, int limit);

    /**
     * Find compatible mentors for a youth user
     *
     * @param youthUserId Youth user ID
     * @param limit Maximum number of mentor recommendations
     * @return List of ranked mentor recommendations
     */
    List<MentorRecommendation> getCompatibleMentors(Long youthUserId, int limit);

    /**
     * Predict success probability for a user applying to an opportunity
     *
     * @param userId User ID
     * @param opportunityId Opportunity ID
     * @return Success probability (0.0 - 1.0)
     */
    double predictSuccessProbability(Long userId, Long opportunityId);

    /**
     * Get user behavior insights and patterns
     *
     * @param userId User ID
     * @return Behavioral insights and recommendations
     */
    UserBehaviorInsights getUserBehaviorInsights(Long userId);

    /**
     * Check if ML models are loaded and healthy
     *
     * @return true if models are operational
     */
    boolean checkMLModelsHealth();

    /**
     * Check if data service connections are healthy
     *
     * @return true if can connect to required services
     */
    boolean checkDataServiceHealth();
}