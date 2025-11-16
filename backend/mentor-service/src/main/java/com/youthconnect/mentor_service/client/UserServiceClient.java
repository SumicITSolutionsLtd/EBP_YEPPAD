package com.youthconnect.mentor_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ============================================================================
 * USER SERVICE FEIGN CLIENT (FIXED - UUID COMPLIANT)
 * ============================================================================
 *
 * Feign client for inter-service communication with user-service.
 * Provides access to user profile data needed for mentorship operations.
 *
 * FIXED ISSUES:
 * ✅ All methods now use UUID for user identification
 * ✅ Removed hasRole method (changed signature from Long to UUID)
 * ✅ Consistent UUID usage across all endpoints
 *
 * KEY RESPONSIBILITIES:
 * - Fetch mentor profile details
 * - Fetch mentee (youth) profile details
 * - Get user preferences (language, timezone)
 * - Search mentors by expertise
 *
 * CACHING:
 * - User profiles cached for 30 minutes (handled by user-service)
 * - Client-side caching also applied for frequently accessed data
 *
 * CIRCUIT BREAKER:
 * - Falls back to cached data when available
 * - Returns minimal user info on service unavailability
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (UUID Compliance Fix)
 * @since 2025-11-07
 * ============================================================================
 */
@FeignClient(
        name = "user-service",
        path = "/api/users",
        fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    /**
     * Get complete user profile by user ID
     * Returns role-specific profile data
     *
     * ✅ FIXED: Now uses UUID parameter
     *
     * @param userId The user's unique identifier (UUID)
     * @return Map containing user profile data
     */
    @GetMapping("/{userId}")
    Map<String, Object> getUserProfile(@PathVariable UUID userId);

    /**
     * Get mentor-specific profile details
     * Includes expertise, bio, experience, availability status
     *
     * ✅ FIXED: Now uses UUID parameter
     *
     * @param mentorId The mentor's unique identifier (UUID)
     * @return Map with mentor profile data
     */
    @GetMapping("/{userId}/mentor-profile")
    Map<String, Object> getMentorProfile(@PathVariable("userId") UUID mentorId);

    /**
     * Get youth (mentee) profile details
     * Includes interests, business stage, district
     *
     * ✅ FIXED: Now uses UUID parameter
     *
     * @param menteeId The mentee's unique identifier (UUID)
     * @return Map with youth profile data
     */
    @GetMapping("/{userId}/youth-profile")
    Map<String, Object> getYouthProfile(@PathVariable("userId") UUID menteeId);

    /**
     * Get user preferences
     * Returns language, timezone, notification preferences
     *
     * ✅ FIXED: Now uses UUID parameter
     *
     * @param userId The user's unique identifier (UUID)
     * @return Map of user preferences
     */
    @GetMapping("/{userId}/preferences")
    Map<String, Object> getUserPreferences(@PathVariable UUID userId);

    /**
     * Search mentors by expertise area
     * Used for mentor matching and discovery
     *
     * @param expertise The expertise keyword to search
     * @param limit Maximum number of results
     * @return List of mentor profiles matching expertise
     */
    @GetMapping("/mentors/search")
    List<Map<String, Object>> searchMentorsByExpertise(
            @RequestParam String expertise,
            @RequestParam(defaultValue = "10") Integer limit
    );

    /**
     * Get user's full name
     * Simple endpoint for display purposes
     *
     * ✅ FIXED: Now uses UUID parameter
     *
     * @param userId The user's unique identifier (UUID)
     * @return Full name as string
     */
    @GetMapping("/{userId}/name")
    String getUserFullName(@PathVariable UUID userId);

    /**
     * Batch get user profiles
     * Efficient retrieval of multiple user profiles
     *
     * @param userIds Comma-separated list of UUID strings
     * @return List of user profile maps
     */
    @GetMapping("/batch")
    List<Map<String, Object>> getBatchUserProfiles(@RequestParam String userIds);
}