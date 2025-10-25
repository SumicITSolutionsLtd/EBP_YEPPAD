package com.youthconnect.user_service.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PostConstruct;
import java.time.Duration;

/**
 * Cache Configuration for Entrepreneurship Booster Platform Uganda User Service
 *
 * This configuration implements a multi-tier caching strategy using Caffeine
 * for high-performance in-memory caching. It includes:
 *
 * - User profile caching for fast authentication
 * - JWT token blacklist for security
 * - Mentor data caching for quick searches
 * - Configuration data caching
 * - Statistics and monitoring integration
 *
 * Cache Tiers:
 * 1. L1 Cache: Frequently accessed data (users, profiles)
 * 2. L2 Cache: Medium frequency data (mentors, configurations)
 * 3. Security Cache: JWT blacklist, session data
 *
 * Features:
 * - Automatic cache warming (via separate service)
 * - Size-based and time-based eviction
 * - Cache statistics and monitoring
 * - Custom key generation strategies
 * - Error handling and fallback mechanisms
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Slf4j
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig implements CachingConfigurer {

    private final ApplicationProperties applicationProperties;

    /**
     * Cache names used throughout the application
     * Centralized definition for consistency
     */
    public static final class CacheNames {
        public static final String USERS = "users";
        public static final String USER_PROFILES = "userProfiles";
        public static final String MENTORS = "mentors";
        public static final String JWT_BLACKLIST = "jwtBlacklist";
        public static final String PHONE_VALIDATION = "phoneValidation";
        public static final String CONFIGURATION = "configuration";
        public static final String RATE_LIMITS = "rateLimits";
        public static final String API_RESPONSES = "apiResponses";

        // Private constructor to prevent instantiation
        private CacheNames() {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }
    }

    /**
     * Validates cache configuration on bean initialization
     * Logs warnings for potentially problematic settings
     */
    @PostConstruct
    public void init() {
        validateCacheConfiguration();
        log.info("Cache configuration initialized successfully");
    }

    /**
     * Primary cache manager with Caffeine implementation
     * Configured with application-specific settings
     *
     * @return Configured CacheManager instance
     */
    @Bean
    @Primary
    @Override
    public CacheManager cacheManager() {
        log.info("Configuring Caffeine cache manager");

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setAllowNullValues(false); // Prevent null value caching

        // Register all cache configurations
        cacheManager.registerCustomCache(CacheNames.USERS, buildUserCache());
        cacheManager.registerCustomCache(CacheNames.USER_PROFILES, buildUserProfileCache());
        cacheManager.registerCustomCache(CacheNames.MENTORS, buildMentorCache());
        cacheManager.registerCustomCache(CacheNames.JWT_BLACKLIST, buildJwtBlacklistCache());
        cacheManager.registerCustomCache(CacheNames.PHONE_VALIDATION, buildPhoneValidationCache());
        cacheManager.registerCustomCache(CacheNames.CONFIGURATION, buildConfigurationCache());
        cacheManager.registerCustomCache(CacheNames.RATE_LIMITS, buildRateLimitCache());
        cacheManager.registerCustomCache(CacheNames.API_RESPONSES, buildApiResponseCache());

        log.info("Configured {} cache regions with statistics enabled", 8);
        return cacheManager;
    }

    /**
     * Cache configuration for user entities
     * High-frequency access, medium TTL
     *
     * Capacity: 5,000 users
     * Write TTL: 30 minutes
     * Access TTL: 10 minutes
     *
     * @return Configured Caffeine cache for users
     */
    private com.github.benmanes.caffeine.cache.Cache<Object, Object> buildUserCache() {
        return Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(Duration.ofMinutes(30))
                .expireAfterAccess(Duration.ofMinutes(10))
                .recordStats()
                .removalListener((key, value, cause) -> {
                    if (cause == RemovalCause.EXPIRED) {
                        log.debug("User cache entry expired for key: {}", key);
                    }
                })
                .build();
    }

    /**
     * Cache configuration for user profiles
     * Frequently accessed during authentication and authorization
     *
     * Capacity: 3,000 profiles
     * Write TTL: 20 minutes
     * Access TTL: 5 minutes
     *
     * @return Configured Caffeine cache for user profiles
     */
    private com.github.benmanes.caffeine.cache.Cache<Object, Object> buildUserProfileCache() {
        return Caffeine.newBuilder()
                .maximumSize(3000)
                .expireAfterWrite(Duration.ofMinutes(20))
                .expireAfterAccess(Duration.ofMinutes(5))
                .recordStats()
                .removalListener((key, value, cause) -> {
                    log.debug("User profile cache entry removed for key: {}, cause: {}", key, cause);
                })
                .build();
    }

    /**
     * Cache configuration for mentor data
     * Used for mentor search and matching functionality
     *
     * Capacity: 1,000 mentors
     * Write TTL: 1 hour
     * Access TTL: 30 minutes
     *
     * @return Configured Caffeine cache for mentors
     */
    private com.github.benmanes.caffeine.cache.Cache<Object, Object> buildMentorCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofHours(1))
                .expireAfterAccess(Duration.ofMinutes(30))
                .recordStats()
                .removalListener((key, value, cause) -> {
                    log.debug("Mentor cache entry removed for key: {}, cause: {}", key, cause);
                })
                .build();
    }

    /**
     * Cache configuration for JWT token blacklist
     * Security-critical cache with short TTL
     *
     * Capacity: 10,000 tokens
     * Write TTL: 24 hours (matches JWT expiration)
     *
     * @return Configured Caffeine cache for JWT blacklist
     */
    private com.github.benmanes.caffeine.cache.Cache<Object, Object> buildJwtBlacklistCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(Duration.ofHours(24)) // Match JWT expiration
                .recordStats()
                .removalListener((key, value, cause) -> {
                    if (cause == RemovalCause.EXPIRED) {
                        log.debug("JWT blacklist entry expired for token: {}", key);
                    }
                })
                .build();
    }

    /**
     * Cache configuration for phone number validation results
     * Reduces external API calls for phone validation
     *
     * Capacity: 2,000 phone numbers
     * Write TTL: 6 hours
     *
     * @return Configured Caffeine cache for phone validation
     */
    private com.github.benmanes.caffeine.cache.Cache<Object, Object> buildPhoneValidationCache() {
        return Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(Duration.ofHours(6))
                .recordStats()
                .build();
    }

    /**
     * Cache configuration for application configuration data
     * Long TTL for relatively static data
     *
     * Capacity: 500 configuration entries
     * Write TTL: 4 hours
     *
     * @return Configured Caffeine cache for configuration
     */
    private com.github.benmanes.caffeine.cache.Cache<Object, Object> buildConfigurationCache() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofHours(4))
                .recordStats()
                .build();
    }

    /**
     * Cache configuration for rate limiting buckets
     * High-frequency access, short TTL
     *
     * Capacity: 10,000 rate limit entries
     * Write TTL: 5 minutes
     * Access TTL: 1 minute
     *
     * @return Configured Caffeine cache for rate limits
     */
    private com.github.benmanes.caffeine.cache.Cache<Object, Object> buildRateLimitCache() {
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .expireAfterAccess(Duration.ofMinutes(1))
                .recordStats()
                .build();
    }

    /**
     * Cache configuration for API response caching
     * Caches expensive computations and external API calls
     *
     * Capacity: 1,000 API responses
     * Write TTL: 15 minutes
     *
     * @return Configured Caffeine cache for API responses
     */
    private com.github.benmanes.caffeine.cache.Cache<Object, Object> buildApiResponseCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(15))
                .recordStats()
                .build();
    }

    /**
     * Custom key generator for cache operations
     * Creates consistent cache keys based on method parameters
     *
     * Format: ClassName:MethodName:param1:param2:...
     * Example: UserServiceImpl:findById:123
     *
     * @return KeyGenerator implementation
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder keyBuilder = new StringBuilder();
            keyBuilder.append(target.getClass().getSimpleName())
                    .append(":")
                    .append(method.getName());

            for (Object param : params) {
                keyBuilder.append(":")
                        .append(param != null ? param.toString() : "null");
            }

            String key = keyBuilder.toString();
            log.trace("Generated cache key: {}", key);
            return key;
        };
    }

    /**
     * Custom cache error handler
     * Ensures cache failures don't break application functionality
     *
     * Strategy: Log errors and continue without cache
     * This prevents cache issues from cascading to application logic
     *
     * @return CacheErrorHandler implementation
     */
    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache get error for cache: {}, key: {}, error: {}",
                        cache.getName(), key, exception.getMessage());
                // Continue without cache - don't break application flow
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Cache put error for cache: {}, key: {}, error: {}",
                        cache.getName(), key, exception.getMessage());
                // Continue without caching - don't break application flow
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache evict error for cache: {}, key: {}, error: {}",
                        cache.getName(), key, exception.getMessage());
                // Continue - eviction failure is not critical
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Cache clear error for cache: {}, error: {}",
                        cache.getName(), exception.getMessage());
                // Continue - clear failure is not critical
            }
        };
    }

    /**
     * Custom cache statistics collector for monitoring and metrics
     * Provides insights into cache performance
     *
     * RENAMED from 'cacheStatsCollector' to avoid conflicts
     *
     * @param cacheManager Spring CacheManager instance
     * @return CustomCacheStatsCollector instance
     */
    @Bean
    public CustomCacheStatsCollector customCacheStatsCollector(CacheManager cacheManager) {
        return new CustomCacheStatsCollector(cacheManager);
    }

    /**
     * Custom cache monitoring service
     * RENAMED from 'cacheMetricsRegistrar' to avoid conflict with Spring Boot Actuator
     *
     * Spring Boot Actuator already provides a 'cacheMetricsRegistrar' bean,
     * so we use a different name for our custom implementation
     *
     * @param cacheManager Spring CacheManager instance
     * @return CustomCacheMonitor instance
     */
    @Bean
    public CustomCacheMonitor customCacheMonitor(CacheManager cacheManager) {
        return new CustomCacheMonitor(cacheManager);
    }

    /**
     * Cache configuration properties validator
     * Ensures cache settings are appropriate for the environment
     */
    private void validateCacheConfiguration() {
        var cacheConfig = applicationProperties.getCache();

        if (cacheConfig.getMaxEntries() > 50000) {
            log.warn("Cache max entries is set very high ({}). Consider reducing for memory efficiency.",
                    cacheConfig.getMaxEntries());
        }

        if (cacheConfig.getTtl() < 60) {
            log.warn("Cache TTL is set very low ({} seconds). This may impact performance.",
                    cacheConfig.getTtl());
        }

        if (!cacheConfig.isEnabled()) {
            log.warn("Caching is disabled. This will significantly impact application performance.");
        }

        log.debug("Cache configuration validated: maxEntries={}, ttl={}s, enabled={}",
                cacheConfig.getMaxEntries(), cacheConfig.getTtl(), cacheConfig.isEnabled());
    }

    /**
     * Custom cache statistics collector for monitoring cache performance
     * Integrates with application metrics system
     *
     * This is our custom implementation, separate from Spring Boot Actuator's metrics
     */
    public static class CustomCacheStatsCollector {

        private final CacheManager cacheManager;

        /**
         * Constructor with CacheManager injection
         *
         * @param cacheManager Spring CacheManager instance
         */
        public CustomCacheStatsCollector(CacheManager cacheManager) {
            this.cacheManager = cacheManager;
        }

        /**
         * Collect cache statistics for all configured caches
         * Can be called periodically to monitor cache health
         */
        public void collectStats() {
            if (cacheManager instanceof CaffeineCacheManager caffeineCacheManager) {
                caffeineCacheManager.getCacheNames().forEach(cacheName -> {
                    Cache cache = caffeineCacheManager.getCache(cacheName);
                    if (cache instanceof org.springframework.cache.caffeine.CaffeineCache caffeineCache) {
                        CacheStats stats = caffeineCache.getNativeCache().stats();

                        log.debug("Cache stats for {}: " +
                                        "hitCount={}, missCount={}, hitRate={:.2f}%, " +
                                        "evictionCount={}, averageLoadTime={:.2f}ms",
                                cacheName,
                                stats.hitCount(),
                                stats.missCount(),
                                stats.hitRate() * 100,
                                stats.evictionCount(),
                                stats.averageLoadPenalty() / 1_000_000.0);
                    }
                });
            }
        }

        /**
         * Get cache statistics for a specific cache
         *
         * @param cacheName Name of the cache
         * @return CacheStats or null if cache not found
         */
        public CacheStats getStatsForCache(String cacheName) {
            if (cacheManager instanceof CaffeineCacheManager caffeineCacheManager) {
                Cache cache = caffeineCacheManager.getCache(cacheName);
                if (cache instanceof org.springframework.cache.caffeine.CaffeineCache caffeineCache) {
                    return caffeineCache.getNativeCache().stats();
                }
            }
            return null;
        }

        /**
         * Get overall cache efficiency metrics
         *
         * @return Overall hit rate across all caches
         */
        public double getOverallHitRate() {
            if (!(cacheManager instanceof CaffeineCacheManager caffeineCacheManager)) {
                return 0.0;
            }

            long totalHits = 0;
            long totalRequests = 0;

            for (String cacheName : caffeineCacheManager.getCacheNames()) {
                Cache cache = caffeineCacheManager.getCache(cacheName);
                if (cache instanceof org.springframework.cache.caffeine.CaffeineCache caffeineCache) {
                    CacheStats stats = caffeineCache.getNativeCache().stats();
                    totalHits += stats.hitCount();
                    totalRequests += stats.requestCount();
                }
            }

            return totalRequests > 0 ? (double) totalHits / totalRequests : 0.0;
        }
    }

    /**
     * Custom cache monitoring service
     * Provides additional monitoring beyond Spring Boot Actuator's built-in metrics
     *
     * This complements (not replaces) Spring Boot Actuator's cache metrics
     */
    public static class CustomCacheMonitor {

        private final CacheManager cacheManager;

        /**
         * Constructor with CacheManager injection
         *
         * @param cacheManager Spring CacheManager instance
         */
        public CustomCacheMonitor(CacheManager cacheManager) {
            this.cacheManager = cacheManager;
            log.debug("Custom cache monitor initialized");
        }

        /**
         * Log detailed cache health information
         * Useful for troubleshooting and performance analysis
         */
        public void logCacheHealth() {
            if (!(cacheManager instanceof CaffeineCacheManager caffeineCacheManager)) {
                log.warn("CacheManager is not an instance of CaffeineCacheManager");
                return;
            }

            log.info("=== Cache Health Report ===");

            caffeineCacheManager.getCacheNames().forEach(cacheName -> {
                Cache cache = caffeineCacheManager.getCache(cacheName);
                if (cache instanceof org.springframework.cache.caffeine.CaffeineCache caffeineCache) {
                    CacheStats stats = caffeineCache.getNativeCache().stats();
                    long size = caffeineCache.getNativeCache().estimatedSize();

                    log.info("Cache '{}': size={}, hitRate={:.2f}%, missRate={:.2f}%, evictions={}",
                            cacheName,
                            size,
                            stats.hitRate() * 100,
                            stats.missRate() * 100,
                            stats.evictionCount());
                }
            });

            log.info("=== End Cache Health Report ===");
        }

        /**
         * Check if any cache is performing poorly
         *
         * @return true if any cache has hit rate below 50%
         */
        public boolean hasPerformanceIssues() {
            if (!(cacheManager instanceof CaffeineCacheManager caffeineCacheManager)) {
                return false;
            }

            for (String cacheName : caffeineCacheManager.getCacheNames()) {
                Cache cache = caffeineCacheManager.getCache(cacheName);
                if (cache instanceof org.springframework.cache.caffeine.CaffeineCache caffeineCache) {
                    CacheStats stats = caffeineCache.getNativeCache().stats();
                    if (stats.requestCount() > 100 && stats.hitRate() < 0.5) {
                        log.warn("Cache '{}' has poor performance: hitRate={:.2f}%",
                                cacheName, stats.hitRate() * 100);
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Get cache size information
         *
         * @return Total estimated size across all caches
         */
        public long getTotalCacheSize() {
            if (!(cacheManager instanceof CaffeineCacheManager caffeineCacheManager)) {
                return 0;
            }

            long totalSize = 0;
            for (String cacheName : caffeineCacheManager.getCacheNames()) {
                Cache cache = caffeineCacheManager.getCache(cacheName);
                if (cache instanceof org.springframework.cache.caffeine.CaffeineCache caffeineCache) {
                    totalSize += caffeineCache.getNativeCache().estimatedSize();
                }
            }
            return totalSize;
        }
    }
}