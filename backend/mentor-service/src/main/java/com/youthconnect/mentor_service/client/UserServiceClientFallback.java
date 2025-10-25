package com.youthconnect.mentor_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================================
 * USER SERVICE FALLBACK
 * ============================================================================
 *
 * Fallback implementation for UserServiceClient.
 * Provides minimal user data when user-service is unavailable.
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-21
 * ============================================================================
 */
@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public Map<String, Object> getUserProfile(Long userId) {
        log.error("Failed to fetch user profile for userId: {}", userId);
        Map<String, Object> fallbackProfile = new HashMap<>();
        fallbackProfile.put("userId", userId);
        fallbackProfile.put("error", "User service unavailable");
        return fallbackProfile;
    }

    @Override
    public Map<String, Object> getMentorProfile(Long mentorId) {
        log.error("Failed to fetch mentor profile for mentorId: {}", mentorId);
        return getUserProfile(mentorId);
    }

    @Override
    public Map<String, Object> getYouthProfile(Long menteeId) {
        log.error("Failed to fetch youth profile for menteeId: {}", menteeId);
        return getUserProfile(menteeId);
    }

    @Override
    public Boolean hasRole(Long userId, String role) {
        log.error("Failed to validate role for userId: {}, role: {}", userId, role);
        return false; // Fail closed for security
    }

    @Override
    public Map<String, Object> getUserPreferences(Long userId) {
        log.error("Failed to fetch preferences for userId: {}", userId);
        Map<String, Object> defaultPrefs = new HashMap<>();
        defaultPrefs.put("language", "en");
        defaultPrefs.put("timezone", "Africa/Kampala");
        return defaultPrefs;
    }

    @Override
    public List<Map<String, Object>> searchMentorsByExpertise(String expertise, Integer limit) {
        log.error("Failed to search mentors by expertise: {}", expertise);
        return Collections.emptyList();
    }

    @Override
    public String getUserFullName(Long userId) {
        log.error("Failed to fetch name for userId: {}", userId);
        return "User " + userId;
    }

    @Override
    public List<Map<String, Object>> getBatchUserProfiles(String userIds) {
        log.error("Failed to fetch batch user profiles: {}", userIds);
        return Collections.emptyList();
    }
}