package com.youthconnect.mentor_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * ============================================================================
 * USER SERVICE FALLBACK (FIXED - UUID COMPLIANT)
 * ============================================================================
 *
 * Fallback implementation for UserServiceClient.
 * Provides minimal user data when user-service is unavailable.
 *
 * FIXED ISSUES:
 * ✅ All method signatures now match interface (UUID parameters)
 * ✅ Removed hasRole method (incompatible signature)
 * ✅ Proper @Override annotations
 *
 * STRATEGY:
 * - Log failure to monitoring system
 * - Don't block user operations
 * - Return minimal fallback data
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (UUID Compliance Fix)
 * @since 2025-11-07
 * ============================================================================
 */
@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public Map<String, Object> getUserProfile(UUID userId) {
        log.error("Failed to fetch user profile for userId: {}", userId);
        Map<String, Object> fallbackProfile = new HashMap<>();
        fallbackProfile.put("userId", userId.toString());
        fallbackProfile.put("error", "User service unavailable");
        fallbackProfile.put("fallback", true);
        return fallbackProfile;
    }

    @Override
    public Map<String, Object> getMentorProfile(UUID mentorId) {
        log.error("Failed to fetch mentor profile for mentorId: {}", mentorId);
        return getUserProfile(mentorId);
    }

    @Override
    public Map<String, Object> getYouthProfile(UUID menteeId) {
        log.error("Failed to fetch youth profile for menteeId: {}", menteeId);
        return getUserProfile(menteeId);
    }

    @Override
    public Map<String, Object> getUserPreferences(UUID userId) {
        log.error("Failed to fetch preferences for userId: {}", userId);
        Map<String, Object> defaultPrefs = new HashMap<>();
        defaultPrefs.put("language", "en");
        defaultPrefs.put("timezone", "Africa/Kampala");
        defaultPrefs.put("fallback", true);
        return defaultPrefs;
    }

    @Override
    public List<Map<String, Object>> searchMentorsByExpertise(String expertise, Integer limit) {
        log.error("Failed to search mentors by expertise: {}", expertise);
        return Collections.emptyList();
    }

    @Override
    public String getUserFullName(UUID userId) {
        log.error("Failed to fetch name for userId: {}", userId);
        return "User " + userId.toString().substring(0, 8);
    }

    @Override
    public List<Map<String, Object>> getBatchUserProfiles(String userIds) {
        log.error("Failed to fetch batch user profiles: {}", userIds);
        return Collections.emptyList();
    }
}