package com.youthconnect.mentor_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * USER SERVICE FEIGN CLIENT
 * ============================================================================
 *
 * Feign client for inter-service communication with user-service.
 * Provides access to user profile data needed for mentorship operations.
 *
 * KEY RESPONSIBILITIES:
 * - Fetch mentor profile details
 * - Fetch mentee (youth) profile details
 * - Validate user roles
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
 * @version 1.0.0
 * @since 2025-01-21
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
     * @param userId The user's unique identifier
     * @return Map containing user profile data
     */
    @GetMapping("/{userId}")
    Map<String, Object> getUserProfile(@PathVariable Long userId);

    /**
     * Get mentor-specific profile details
     * Includes expertise, bio, experience, availability status
     *
     * @param mentorId The mentor's user ID
     * @return Map with mentor profile data
     */
    @GetMapping("/{userId}/mentor-profile")
    Map<String, Object> getMentorProfile(@PathVariable("userId") Long mentorId);

    /**
     * Get youth (mentee) profile details
     * Includes interests, business stage, district
     *
     * @param menteeId The mentee's user ID
     * @return Map with youth profile data
     */
    @GetMapping("/{userId}/youth-profile")
    Map<String, Object> getYouthProfile(@PathVariable("userId") Long menteeId);

    /**
     * Validate user role
     * Checks if user has specified role
     *
     * @param userId The user ID to check
     * @param role The role to validate (MENTOR, YOUTH, etc.)
     * @return true if user has the role
     */
    @GetMapping("/{userId}/has-role")
    Boolean hasRole(@PathVariable Long userId, @RequestParam String role);

    /**
     * Get user preferences
     * Returns language, timezone, notification preferences
     *
     * @param userId The user ID
     * @return Map of user preferences
     */
    @GetMapping("/{userId}/preferences")
    Map<String, Object> getUserPreferences(@PathVariable Long userId);

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
     * @param userId The user ID
     * @return Full name as string
     */
    @GetMapping("/{userId}/name")
    String getUserFullName(@PathVariable Long userId);

    /**
     * Batch get user profiles
     * Efficient retrieval of multiple user profiles
     *
     * @param userIds Comma-separated list of user IDs
     * @return List of user profile maps
     */
    @GetMapping("/batch")
    List<Map<String, Object>> getBatchUserProfiles(@RequestParam String userIds);
}