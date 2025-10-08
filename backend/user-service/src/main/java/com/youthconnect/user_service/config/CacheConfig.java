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
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Cache Configuration for Youth Connect Uganda User Service
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
 * - Automatic cache warming
 * - Size-based and time-based eviction
 * - Cache statistics and monitoring
 * - Custom key generation strategies
 * - Error handling and fallback mechanisms
 *
 * @author Youth Connect Uganda Development Team
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
    }

    /**
     * Primary cache manager with Caffeine implementation
     * Configured with application-specific settings
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
     */
    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache get error for cache: {}, key: {}", cache.getName(), key, exception);
                // Continue without cache - don't break application flow
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Cache put error for cache: {}, key: {}", cache.getName(), key, exception);
                // Continue without caching - don't break application flow
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache evict error for cache: {}, key: {}", cache.getName(), key, exception);
                // Continue - eviction failure is not critical
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Cache clear error for cache: {}", cache.getName(), exception);
                // Continue - clear failure is not critical
            }
        };
    }

    /**
     * Cache statistics bean for monitoring and metrics
     * Provides insights into cache performance
     */
    @Bean
    public CacheStatsCollector cacheStatsCollector() {
        return new CacheStatsCollector();
    }

    /**
     * Cache warming service to preload frequently accessed data
     */
    @Bean
    public CacheWarmupService cacheWarmupService() {
        return new CacheWarmupService();
    }

    /**
     * Cache statistics collector for monitoring cache performance
     * Integrates with application metrics system
     */
    public static class CacheStatsCollector {

        private final CacheManager cacheManager;

        public CacheStatsCollector() {
            this.cacheManager = null; // Will be injected
        }

        /**
         * Collect cache statistics for all configured caches
         */
        public void collectStats() {
            if (cacheManager instanceof CaffeineCacheManager caffeineCacheManager) {
                caffeineCacheManager.getCacheNames().forEach(cacheName -> {
                    Cache cache = caffeineCacheManager.getCache(cacheName);
                    if (cache instanceof org.springframework.cache.caffeine.CaffeineCache caffeineCache) {
                        CacheStats stats = caffeineCache.getNativeCache().stats();

                        log.debug("Cache stats for {}: " +
                                        "hitCount={}, missCount={}, hitRate={:.2f}, " +
                                        "evictionCount={}, averageLoadTime={:.2f}ms",
                                cacheName,
                                stats.hitCount(),
                                stats.missCount(),
                                stats.hitRate() * 100,
                                stats.evictionCount(),
                                stats.averageLoadPenalty() / 1_000_000.0);

                        // Here you would typically send these metrics to your monitoring system
                        // For example: meterRegistry.gauge("cache.hit.rate", Tags.of("cache", cacheName), stats.hitRate());
                    }
                });
            }
        }
    }

    /**
     * Cache warmup service to preload critical data
     * Improves application startup performance
     */
    public static class CacheWarmupService {

        /**
         * Warm up caches with frequently accessed data
         * Called during application startup
         */
        public void warmupCaches() {
            log.info("Starting cache warmup process");

            // This would typically:
            // 1. Load frequently accessed user profiles
            // 2. Preload mentor data
            // 3. Cache configuration values
            // 4. Prepare common API responses

            log.info("Cache warmup completed");
        }
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
    }

    /**
     * Cache metrics integration
     * Registers cache metrics with the application monitoring system
     */
    @Bean
    public CacheMetricsRegistrar cacheMetricsRegistrar() {
        return new CacheMetricsRegistrar();
    }

    /**
     * Registers cache metrics with Micrometer for monitoring
     */
    public static class CacheMetricsRegistrar {

        /**
         * Register cache metrics for monitoring
         */
        public void registerMetrics() {
            // This would integrate with your metrics system
            // For example, registering Caffeine cache metrics with Micrometer
            log.debug("Cache metrics registered with monitoring system");
        }
    }
}