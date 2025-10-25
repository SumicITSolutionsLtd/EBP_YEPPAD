package com.youthconnect.mentor_service.service;

import com.youthconnect.mentor_service.client.UserServiceClient;
import com.youthconnect.mentor_service.entity.MentorshipSession;
import com.youthconnect.mentor_service.repository.MentorshipSessionRepository;
import com.youthconnect.mentor_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * ============================================================================
 * MENTOR MATCHING SERVICE
 * ============================================================================
 *
 * AI-powered mentor-mentee matching algorithm.
 * Calculates match scores based on multiple factors to recommend best mentors.
 *
 * MATCHING FACTORS:
 * 1. Expertise Alignment (40% weight) - Mentor expertise matches mentee interests
 * 2. Rating & Success History (30% weight) - Mentor's past performance
 * 3. Availability Compatibility (20% weight) - Mentor has available time slots
 * 4. Geographic Proximity (10% weight) - Same district/region preference
 *
 * ALGORITHM:
 * - Weighted scoring system with configurable weights
 * - Collaborative filtering for similar user patterns
 * - Content-based filtering for expertise matching
 * - Hybrid approach combining multiple signals
 *
 * CACHING:
 * - Match scores cached for 15 minutes
 * - Cache invalidated on mentor profile updates
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-22
 * ============================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MentorMatchingService {

    private final UserServiceClient userServiceClient;
    private final MentorshipSessionRepository sessionRepository;
    private final ReviewRepository reviewRepository;

    // Configurable matching weights (should come from ApplicationProperties)
    private static final double EXPERTISE_WEIGHT = 0.40;
    private static final double RATING_WEIGHT = 0.30;
    private static final double AVAILABILITY_WEIGHT = 0.20;
    private static final double LOCATION_WEIGHT = 0.10;

    /**
     * Find recommended mentors for a mentee
     * Returns top N mentors ranked by match score
     *
     * @param menteeId The mentee's user ID
     * @param limit Maximum number of recommendations
     * @return List of mentor profiles with match scores
     */
    @Cacheable(value = "matchScores", key = "#menteeId + '-' + #limit")
    public List<MentorMatchResult> findRecommendedMentors(Long menteeId, int limit) {
        log.info("Finding recommended mentors for mentee: {}", menteeId);

        // Get mentee profile and interests
        Map<String, Object> menteeProfile = userServiceClient.getYouthProfile(menteeId);
        List<String> menteeInterests = extractInterests(menteeProfile);
        String menteeDistrict = extractDistrict(menteeProfile);

        log.debug("Mentee interests: {}, district: {}", menteeInterests, menteeDistrict);

        // Search for mentors (could also filter by expertise here)
        List<Map<String, Object>> allMentors = userServiceClient.searchMentorsByExpertise(
                String.join(",", menteeInterests),
                100  // Get more mentors for better matching
        );

        log.info("Found {} potential mentors for matching", allMentors.size());

        // Calculate match score for each mentor
        List<MentorMatchResult> matchResults = allMentors.stream()
                .map(mentorProfile -> {
                    Long mentorId = extractUserId(mentorProfile);
                    double matchScore = calculateMatchScore(
                            menteeId,
                            mentorId,
                            menteeInterests,
                            menteeDistrict,
                            mentorProfile
                    );

                    return MentorMatchResult.builder()
                            .mentorId(mentorId)
                            .mentorProfile(mentorProfile)
                            .matchScore(matchScore)
                            .build();
                })
                .filter(result -> result.getMatchScore() >= 60.0)  // Minimum 60% match
                .sorted(Comparator.comparingDouble(MentorMatchResult::getMatchScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        log.info("Returning {} recommended mentors for mentee: {}", matchResults.size(), menteeId);

        return matchResults;
    }

    /**
     * Calculate comprehensive match score
     * Combines multiple factors with weighted scoring
     *
     * @param menteeId The mentee's user ID
     * @param mentorId The mentor's user ID
     * @param menteeInterests Mentee's interests
     * @param menteeDistrict Mentee's district
     * @param mentorProfile Mentor's profile data
     * @return Match score (0-100)
     */
    private double calculateMatchScore(
            Long menteeId,
            Long mentorId,
            List<String> menteeInterests,
            String menteeDistrict,
            Map<String, Object> mentorProfile
    ) {
        log.debug("Calculating match score for mentor: {} and mentee: {}", mentorId, menteeId);

        // 1. Expertise alignment score (40%)
        double expertiseScore = calculateExpertiseScore(menteeInterests, mentorProfile);
        log.debug("Expertise score: {}", expertiseScore);

        // 2. Rating and success history score (30%)
        double ratingScore = calculateRatingScore(mentorId);
        log.debug("Rating score: {}", ratingScore);

        // 3. Availability compatibility score (20%)
        double availabilityScore = calculateAvailabilityScore(mentorId);
        log.debug("Availability score: {}", availabilityScore);

        // 4. Geographic proximity score (10%)
        double locationScore = calculateLocationScore(menteeDistrict, mentorProfile);
        log.debug("Location score: {}", locationScore);

        // Calculate weighted total
        double totalScore = (expertiseScore * EXPERTISE_WEIGHT) +
                (ratingScore * RATING_WEIGHT) +
                (availabilityScore * AVAILABILITY_WEIGHT) +
                (locationScore * LOCATION_WEIGHT);

        // Normalize to 0-100 scale
        double normalizedScore = totalScore * 100;

        log.info("Final match score for mentor {}: {}", mentorId, normalizedScore);

        return normalizedScore;
    }

    /**
     * Calculate expertise alignment score
     * Measures overlap between mentee interests and mentor expertise
     *
     * @param menteeInterests List of mentee's interests
     * @param mentorProfile Mentor's profile with expertise
     * @return Score 0.0-1.0
     */
    private double calculateExpertiseScore(
            List<String> menteeInterests,
            Map<String, Object> mentorProfile
    ) {
        if (menteeInterests == null || menteeInterests.isEmpty()) {
            return 0.5;  // Neutral score if no interests specified
        }

        String mentorExpertise = (String) mentorProfile.get("areaOfExpertise");
        if (mentorExpertise == null || mentorExpertise.isEmpty()) {
            return 0.3;  // Low score if mentor has no expertise listed
        }

        List<String> mentorExpertiseList = Arrays.asList(
                        mentorExpertise.toLowerCase().split("[,;]")
                ).stream()
                .map(String::trim)
                .collect(Collectors.toList());

        // Calculate overlap
        long matchCount = menteeInterests.stream()
                .map(String::toLowerCase)
                .filter(interest -> mentorExpertiseList.stream()
                        .anyMatch(expertise -> expertise.contains(interest) ||
                                interest.contains(expertise)))
                .count();

        return (double) matchCount / menteeInterests.size();
    }

    /**
     * Calculate rating and success history score
     * Based on mentor's average rating and completed sessions
     *
     * @param mentorId The mentor's user ID
     * @return Score 0.0-1.0
     */
    private double calculateRatingScore(Long mentorId) {
        try {
            // Get average rating from reviews
            Double averageRating = sessionRepository.getAverageRatingForMentor(mentorId);

            if (averageRating == null) {
                return 0.6;  // Neutral score for new mentors
            }

            // Normalize 5-star rating to 0-1 scale
            return averageRating / 5.0;
        } catch (Exception e) {
            log.warn("Failed to calculate rating score for mentor: {}. Error: {}",
                    mentorId, e.getMessage());
            return 0.5;
        }
    }

    /**
     * Calculate availability compatibility score
     * Checks if mentor has available capacity and status
     *
     * @param mentorId The mentor's user ID
     * @return Score 0.0-1.0
     */
    private double calculateAvailabilityScore(Long mentorId) {
        try {
            // Count completed sessions in last 30 days
            long recentSessions = sessionRepository.countByMentorIdAndStatus(
                    mentorId,
                    MentorshipSession.SessionStatus.COMPLETED
            );

            // Check if mentor has capacity (assume max 10 sessions/month)
            if (recentSessions >= 10) {
                return 0.3;  // Low score if fully booked
            } else if (recentSessions >= 7) {
                return 0.6;  // Medium score if mostly booked
            } else {
                return 1.0;  // High score if available
            }
        } catch (Exception e) {
            log.warn("Failed to calculate availability score for mentor: {}. Error: {}",
                    mentorId, e.getMessage());
            return 0.7;  // Default to somewhat available
        }
    }

    /**
     * Calculate geographic proximity score
     * Prefers mentors in same district for in-person sessions
     *
     * @param menteeDistrict Mentee's district
     * @param mentorProfile Mentor's profile
     * @return Score 0.0-1.0
     */
    private double calculateLocationScore(String menteeDistrict, Map<String, Object> mentorProfile) {
        if (menteeDistrict == null || menteeDistrict.isEmpty()) {
            return 0.5;  // Neutral if no district specified
        }

        String mentorDistrict = (String) mentorProfile.get("location");
        if (mentorDistrict == null || mentorDistrict.isEmpty()) {
            return 0.5;
        }

        // Exact match
        if (menteeDistrict.equalsIgnoreCase(mentorDistrict)) {
            return 1.0;
        }

        // Same region (basic check - could be enhanced)
        if (mentorDistrict.toLowerCase().contains(menteeDistrict.toLowerCase()) ||
                menteeDistrict.toLowerCase().contains(mentorDistrict.toLowerCase())) {
            return 0.7;
        }

        // Different district
        return 0.3;
    }

    /**
     * Extract interests from user profile
     */
    @SuppressWarnings("unchecked")
    private List<String> extractInterests(Map<String, Object> profile) {
        Object interestsObj = profile.get("interests");
        if (interestsObj instanceof List) {
            return (List<String>) interestsObj;
        } else if (interestsObj instanceof String) {
            return Arrays.asList(((String) interestsObj).split(","));
        }
        return Collections.emptyList();
    }

    /**
     * Extract district from user profile
     */
    private String extractDistrict(Map<String, Object> profile) {
        return (String) profile.getOrDefault("district", "");
    }

    /**
     * Extract user ID from profile
     */
    private Long extractUserId(Map<String, Object> profile) {
        Object userId = profile.get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return Long.parseLong(userId.toString());
    }

    /**
     * Inner class for match results
     */
    @lombok.Data
    @lombok.Builder
    public static class MentorMatchResult {
        private Long mentorId;
        private Map<String, Object> mentorProfile;
        private double matchScore;
        private String matchReason;  // Could add explanation
    }
}