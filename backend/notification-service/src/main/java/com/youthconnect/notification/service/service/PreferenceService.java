package com.youthconnect.notification.service.service;

import com.youthconnect.notification.service.dto.NotificationPreferences;
import com.youthconnect.notification.service.dto.NotificationPreferences.NotificationCategory;
import com.youthconnect.notification.service.dto.NotificationPreferences.NotificationFrequency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * NOTIFICATION PREFERENCE SERVICE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Manages user notification preferences including:
 * - Channel preferences (SMS, Email, Push, In-app)
 * - Notification frequency (immediate, daily digest, weekly)
 * - Quiet hours (do not disturb periods)
 * - Category preferences (updates, alerts, reminders, marketing)
 *
 * Uses Redis for fast preference lookups with 1-hour TTL.
 *
 * @author Douglas Kings Kato
 * @version 1.0
 * @since 2025-10-20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PreferenceService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PREFERENCE_KEY_PREFIX = "notification:preferences:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    /**
     * Get user notification preferences.
     *
     * Lookup priority:
     * 1. Check Redis cache (fast)
     * 2. If not cached, return default preferences
     * 3. Cache result for 1 hour
     *
     * @param userId User ID
     * @return User preferences or defaults
     */
    public NotificationPreferences getUserPreferences(Long userId) {
        String cacheKey = PREFERENCE_KEY_PREFIX + userId;

        try {
            // Try cache first
            Object cached = redisTemplate.opsForValue().get(cacheKey);

            if (cached instanceof NotificationPreferences) {
                log.debug("✅ Preferences cache hit for user: {}", userId);
                return (NotificationPreferences) cached;
            }

            // Cache miss - return defaults
            log.debug("⚠️ Preferences cache miss for user: {} - using defaults", userId);
            NotificationPreferences defaults = getDefaultPreferences(userId);

            // Cache for future requests
            redisTemplate.opsForValue().set(cacheKey, defaults, CACHE_TTL);

            return defaults;

        } catch (Exception e) {
            log.error("❌ Error retrieving preferences for user {}: {}", userId, e.getMessage());
            return getDefaultPreferences(userId);
        }
    }

    /**
     * Update user notification preferences.
     *
     * Validates preferences and updates cache.
     *
     * @param preferences User preferences to save
     */
    public void updateUserPreferences(NotificationPreferences preferences) {
        if (preferences == null || preferences.getUserId() == null) {
            throw new IllegalArgumentException("Preferences and user ID cannot be null");
        }

        String cacheKey = PREFERENCE_KEY_PREFIX + preferences.getUserId();

        try {
            // Validate preferences
            validatePreferences(preferences);

            // Update cache
            redisTemplate.opsForValue().set(cacheKey, preferences, CACHE_TTL);

            log.info("✅ Preferences updated for user: {}", preferences.getUserId());

        } catch (Exception e) {
            log.error("❌ Error updating preferences for user {}: {}",
                    preferences.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Failed to update notification preferences", e);
        }
    }

    /**
     * Check if user should receive notification on a specific channel for a category.
     *
     * @param userId User ID
     * @param channel Channel (SMS, EMAIL, PUSH)
     * @param category Notification category
     * @return true if notification should be sent
     */
    public boolean shouldSendNotification(Long userId, String channel, NotificationCategory category) {
        NotificationPreferences prefs = getUserPreferences(userId);

        // Check if category is enabled
        if (!prefs.isCategoryEnabled(category)) {
            log.debug("⛔ Category {} disabled for user: {}", category, userId);
            return false;
        }

        // Check quiet hours for SMS/Push
        if (("SMS".equals(channel) || "PUSH".equals(channel)) && prefs.isWithinQuietHours(LocalTime.now())) {
            log.debug("⛔ Quiet hours active for user: {}", userId);
            return false;
        }

        // Check channel preference
        boolean channelEnabled = switch (channel.toUpperCase()) {
            case "SMS" -> prefs.isSmsEnabledForCategory(category);
            case "EMAIL" -> prefs.isEmailEnabledForCategory(category);
            case "PUSH" -> prefs.isPushEnabledForCategory(category);
            default -> true;
        };

        if (!channelEnabled) {
            log.debug("⛔ Channel {} disabled for user: {}", channel, userId);
        }

        return channelEnabled;
    }

    /**
     * Clear user preferences cache (force refresh on next request).
     *
     * @param userId User ID
     */
    public void clearPreferencesCache(Long userId) {
        String cacheKey = PREFERENCE_KEY_PREFIX + userId;
        redisTemplate.delete(cacheKey);
        log.info("🗑️ Preferences cache cleared for user: {}", userId);
    }

    /**
     * Get default notification preferences for new users.
     *
     * @param userId User ID
     * @return Default preferences
     */
    private NotificationPreferences getDefaultPreferences(Long userId) {
        return NotificationPreferences.builder()
                .userId(userId)
                .smsEnabled(true)
                .emailEnabled(true)
                .pushEnabled(true)
                .inAppEnabled(true)
                .frequency(NotificationFrequency.IMMEDIATE)
                .quietHoursEnabled(false)
                .quietHoursStart(LocalTime.of(22, 0)) // 10 PM
                .quietHoursEnd(LocalTime.of(7, 0))     // 7 AM
                .enabledCategories(Set.of(
                        NotificationCategory.UPDATES,
                        NotificationCategory.ALERTS,
                        NotificationCategory.REMINDERS
                ))
                .preferredLanguage("EN")
                .build();
    }

    /**
     * Validate notification preferences.
     *
     * @param prefs Preferences to validate
     * @throws IllegalArgumentException if invalid
     */
    private void validatePreferences(NotificationPreferences prefs) {
        if (prefs.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (!prefs.hasAnyChannelEnabled()) {
            throw new IllegalArgumentException("At least one notification channel must be enabled");
        }

        if (prefs.isQuietHoursEnabled()) {
            if (prefs.getQuietHoursStart() == null || prefs.getQuietHoursEnd() == null) {
                throw new IllegalArgumentException("Quiet hours times must be specified when enabled");
            }
        }

        if (prefs.getFrequency() == NotificationFrequency.DAILY_DIGEST ||
                prefs.getFrequency() == NotificationFrequency.WEEKLY_DIGEST) {
            if (prefs.getDigestTime() == null) {
                throw new IllegalArgumentException("Digest time must be specified for digest frequency");
            }
        }
    }
}