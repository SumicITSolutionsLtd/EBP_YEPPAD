package com.youthconnect.mentor_service.service;

import com.youthconnect.mentor_service.client.UserServiceClient;
import com.youthconnect.mentor_service.entity.MentorshipSession;
import com.youthconnect.mentor_service.repository.MentorshipSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ============================================================================
 * MENTOR MATCHING SERVICE (FIXED - UUID COMPLIANT)
 * ============================================================================
 *
 * AI-powered mentor-mentee matching algorithm.
 * Calculates match scores based on multiple factors to recommend best mentors.
 *
 * FIXED ISSUES:
 * ✅ All UUID conversion methods removed
 * ✅ Consistent UUID usage throughout
 * ✅ Proper type handling for user service calls
 *
 * MATCHING FACTORS:
 * 1. Expertise Alignment (40% weight) - Mentor expertise matches mentee interests
 * 2. Rating & Success History (30% weight) - Mentor's past performance
 * 3. Availability Compatibility (20% weight) - Mentor has available time slots
 * 4. Geographic Proximity (10% weight) - Same district/region preference
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (UUID Compliance Fix)
 * @since 2025-11-07
 * ============================================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MentorMatchingService {

    private final UserServiceClient userServiceClient;
    private final MentorshipSessionRepository sessionRepository;

    // Configurable matching weights
    private static final double EXPERTISE_WEIGHT = 0.40;
    private static final double RATING_WEIGHT = 0.30;
    private static final double AVAILABILITY_WEIGHT = 0.20;
    private static final double LOCATION_WEIGHT = 0.10;

    /**
     * Find recommended mentors for a mentee
     * Returns top N mentors ranked by match score
     *
     * ✅ FIXED: Now accepts and uses UUID consistently
     *
     * @param menteeId The mentee's user UUID
     * @param limit Maximum number of recommendations
     * @return List of mentor profiles with match scores
     */
    @Cacheable(value = "matchScores", key = "#menteeId + '-' + #limit")
    public List<MentorMatchResult> findRecommendedMentors(UUID menteeId, int limit) {
        log.info("Finding recommended mentors for mentee: {}", menteeId);

        // Get mentee profile and interests
        Map<String, Object> menteeProfile = userServiceClient.getYouthProfile(menteeId);
        List<String> menteeInterests = extractInterests(menteeProfile);
        String menteeDistrict = extractDistrict(menteeProfile);

        log.debug("Mentee interests: {}, district: {}", menteeInterests, menteeDistrict);

        // Search for mentors
        List<Map<String, Object>> allMentors = userServiceClient.searchMentorsByExpertise(
                String.join(",", menteeInterests),
                100  // Get more mentors for better matching
        );

        log.info("Found {} potential mentors for matching", allMentors.size());

        // Calculate match score for each mentor
        List<MentorMatchResult> matchResults = allMentors.stream()
                .map(mentorProfile -> {
                    UUID mentorId = extractUserId(mentorProfile);

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
     * ✅ FIXED: Now uses UUID for identifiers consistently
     */
    private double calculateMatchScore(
            UUID menteeId,
            UUID mentorId,
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
     */
    private double calculateExpertiseScore(
            List<String> menteeInterests,
            Map<String, Object> mentorProfile
    ) {
        if (menteeInterests == null || menteeInterests.isEmpty()) {
            return 0.5;
        }

        String mentorExpertise = (String) mentorProfile.get("areaOfExpertise");
        if (mentorExpertise == null || mentorExpertise.isEmpty()) {
            return 0.3;
        }

        List<String> mentorExpertiseList = Arrays.stream(
                        mentorExpertise.toLowerCase().split("[,;]")
                )
                .map(String::trim)
                .collect(Collectors.toList());

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
     *
     * ✅ FIXED: Now uses UUID parameter
     */
    private double calculateRatingScore(UUID mentorId) {
        try {
            Double averageRating = sessionRepository.getAverageRatingForMentor(mentorId);

            if (averageRating == null) {
                return 0.6;
            }

            return averageRating / 5.0;
        } catch (Exception e) {
            log.warn("Failed to calculate rating score for mentor: {}. Error: {}",
                    mentorId, e.getMessage());
            return 0.5;
        }
    }

    /**
     * Calculate availability compatibility score
     *
     * ✅ FIXED: Now uses UUID parameter
     */
    private double calculateAvailabilityScore(UUID mentorId) {
        try {
            long recentSessions = sessionRepository.countByMentorIdAndStatus(
                    mentorId,
                    MentorshipSession.SessionStatus.COMPLETED
            );

            if (recentSessions >= 10) {
                return 0.3;
            } else if (recentSessions >= 7) {
                return 0.6;
            } else {
                return 1.0;
            }
        } catch (Exception e) {
            log.warn("Failed to calculate availability score for mentor: {}. Error: {}",
                    mentorId, e.getMessage());
            return 0.7;
        }
    }

    /**
     * Calculate geographic proximity score
     */
    private double calculateLocationScore(String menteeDistrict, Map<String, Object> mentorProfile) {
        if (menteeDistrict == null || menteeDistrict.isEmpty()) {
            return 0.5;
        }

        String mentorDistrict = (String) mentorProfile.get("location");
        if (mentorDistrict == null || mentorDistrict.isEmpty()) {
            return 0.5;
        }

        if (menteeDistrict.equalsIgnoreCase(mentorDistrict)) {
            return 1.0;
        }

        if (mentorDistrict.toLowerCase().contains(menteeDistrict.toLowerCase()) ||
                menteeDistrict.toLowerCase().contains(mentorDistrict.toLowerCase())) {
            return 0.7;
        }

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
     *
     * ✅ FIXED: Now properly extracts UUID from profile map
     */
    private UUID extractUserId(Map<String, Object> profile) {
        Object userId = profile.get("userId");
        if (userId instanceof UUID) {
            return (UUID) userId;
        } else if (userId instanceof String) {
            return UUID.fromString((String) userId);
        } else if (userId instanceof Number) {
            // Handle legacy Long IDs by creating deterministic UUID
            long id = ((Number) userId).longValue();
            return new UUID(id, 0L);
        }
        throw new IllegalArgumentException("Cannot extract UUID from profile: " + userId);
    }

    /**
     * Inner class for match results
     * ✅ FIXED: Uses UUID for mentorId
     */
    @lombok.Data
    @lombok.Builder
    public static class MentorMatchResult {
        private UUID mentorId;
        private Map<String, Object> mentorProfile;
        private double matchScore;
        private String matchReason;
    }
}