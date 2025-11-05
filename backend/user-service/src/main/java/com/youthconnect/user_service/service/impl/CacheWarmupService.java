package com.youthconnect.user_service.service.impl;

import com.youthconnect.user_service.entity.Role;
import com.youthconnect.user_service.entity.User;
import com.youthconnect.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Cache Warmup Service for Youth Connect Uganda User Service
 *
 * Responsibilities:
 * - Pre-loads frequently accessed data into cache on application startup
 * - Periodically refreshes cache to ensure data freshness
 * - Manages cache eviction and reload strategies
 * - Monitors cache performance and efficiency
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheWarmupService {

    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    /**
     * Warm up caches on application startup
     * Pre-loads critical data to improve first-request performance
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmupCachesOnStartup() {
        log.info("Starting cache warmup process...");

        try {
            warmupAdminUsers();
            warmupActiveMentors();
            warmupSystemUsers();
            warmupUserCounts();

            log.info("Cache warmup completed successfully");

        } catch (Exception e) {
            log.error("Cache warmup failed: {}", e.getMessage(), e);
        }
    }

    /**
     * Periodic cache refresh to ensure data freshness
     * Runs every 30 minutes in production
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30 minutes
    public void refreshCaches() {
        log.debug("Refreshing caches...");

        try {
            refreshUserCaches();
            refreshMentorCaches();
            refreshStatisticsCaches();

            log.debug("Cache refresh completed");

        } catch (Exception e) {
            log.warn("Cache refresh failed: {}", e.getMessage());
        }
    }

    /**
     * Warm up admin users cache
     * Admin users are frequently accessed for system operations
     */
    private void warmupAdminUsers() {
        log.debug("Warming up admin users cache...");

        try {
            List<User> adminUsers = userRepository.findByRoleAndIsActiveTrue(Role.ADMIN);
            log.info("Pre-loaded {} admin users into cache", adminUsers.size());

        } catch (Exception e) {
            log.warn("Failed to warm up admin users cache: {}", e.getMessage());
        }
    }

    /**
     * Warm up active mentors cache
     * Mentors are frequently accessed for matching and discovery
     */
    private void warmupActiveMentors() {
        log.debug("Warming up active mentors cache...");

        try {
            List<User> activeMentors = userRepository.findByRoleAndIsActiveTrue(Role.MENTOR);
            log.info("Pre-loaded {} active mentors into cache", activeMentors.size());

        } catch (Exception e) {
            log.warn("Failed to warm up mentors cache: {}", e.getMessage());
        }
    }

    /**
     * Warm up system users cache
     * System users are used for internal operations and auditing
     */
    private void warmupSystemUsers() {
        log.debug("Warming up system users cache...");

        try {
            // Cache users with specific roles that are frequently accessed
            List<Role> frequentlyAccessedRoles = List.of(Role.NGO, Role.FUNDER, Role.SERVICE_PROVIDER);

            for (Role role : frequentlyAccessedRoles) {
                List<User> roleUsers = userRepository.findByRoleAndIsActiveTrue(role);
                log.debug("Pre-loaded {} {} users into cache", roleUsers.size(), role);
            }

        } catch (Exception e) {
            log.warn("Failed to warm up system users cache: {}", e.getMessage());
        }
    }

    /**
     * Warm up user count statistics cache
     * Frequently used for dashboard and reporting
     */
    private void warmupUserCounts() {
        log.debug("Warming up user count statistics cache...");

        try {
            // Pre-calculate and cache user counts by role
            for (Role role : Role.values()) {
                long count = userRepository.countActiveUsersByRole(role);
                log.debug("Pre-cached user count for {}: {}", role, count);
            }

        } catch (Exception e) {
            log.warn("Failed to warm up user count cache: {}", e.getMessage());
        }
    }

    /**
     * Refresh user-related caches
     */
    private void refreshUserCaches() {
        try {
            // Evict and reload user caches
            var usersCache = cacheManager.getCache("users");
            var userProfilesCache = cacheManager.getCache("userProfiles");

            if (usersCache != null) {
                usersCache.clear();
            }
            if (userProfilesCache != null) {
                userProfilesCache.clear();
            }

            log.debug("User caches refreshed");

        } catch (Exception e) {
            log.warn("Failed to refresh user caches: {}", e.getMessage());
        }
    }

    /**
     * Refresh mentor-related caches
     */
    private void refreshMentorCaches() {
        try {
            var mentorsCache = cacheManager.getCache("mentors");
            if (mentorsCache != null) {
                mentorsCache.clear();

                // Reload active mentors
                List<User> activeMentors = userRepository.findByRoleAndIsActiveTrue(Role.MENTOR);
                log.debug("Refreshed {} mentors in cache", activeMentors.size());
            }

        } catch (Exception e) {
            log.warn("Failed to refresh mentor caches: {}", e.getMessage());
        }
    }

    /**
     * Refresh statistics caches
     */
    private void refreshStatisticsCaches() {
        try {
            // Refresh user count statistics
            warmupUserCounts();
            log.debug("Statistics caches refreshed");

        } catch (Exception e) {
            log.warn("Failed to refresh statistics caches: {}", e.getMessage());
        }
    }

    /**
     * Manual cache eviction for specific user
     * Useful when user data changes and cache needs immediate update
     */
    public void evictUserCache(Long userId) {
        try {
            var usersCache = cacheManager.getCache("users");
            var userProfilesCache = cacheManager.getCache("userProfiles");

            if (usersCache != null) {
                usersCache.evict(userId);
            }
            if (userProfilesCache != null) {
                userProfilesCache.evict(userId);
            }

            log.debug("Evicted cache for user ID: {}", userId);

        } catch (Exception e) {
            log.warn("Failed to evict cache for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Clear all caches (for maintenance or emergency situations)
     */
    public void clearAllCaches() {
        log.info("Clearing all caches...");

        try {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });

            log.info("All caches cleared successfully");

        } catch (Exception e) {
            log.error("Failed to clear caches: {}", e.getMessage(), e);
        }
    }

    /**
     * Get cache statistics for monitoring
     */
    public void logCacheStatistics() {
        log.debug("Cache statistics:");

        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache instanceof org.springframework.cache.caffeine.CaffeineCache caffeineCache) {
                var nativeCache = caffeineCache.getNativeCache();
                log.debug("Cache '{}': size={}, stats={}",
                        cacheName, nativeCache.estimatedSize(), nativeCache.stats());
            }
        });
    }
}