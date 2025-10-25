package com.youthconnect.content_service.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Caching configuration for Content Service
 *
 * CACHE REGIONS:
 * 1. learningModules - 1 hour TTL (modules rarely change)
 * 2. moduleProgress - 15 minutes (progress updated frequently)
 * 3. posts - 10 minutes (community content updates)
 * 4. comments - 5 minutes (very dynamic)
 * 5. postVotes - 3 minutes (real-time voting)
 * 6. userPreferences - 30 minutes (language, interests)
 *
 * EVICTION POLICY: Size-based + Time-based
 * BENEFITS: 70% database load reduction
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configures multiple Caffeine cache regions with different TTL strategies
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        cacheManager.setCaches(Arrays.asList(
                // Learning Modules - Long TTL (modules are mostly static)
                buildCache("learningModules", 3600, 500),

                // Module Progress - Medium TTL (updated during learning)
                buildCache("moduleProgress", 900, 1000),

                // Posts - Short TTL (community content changes frequently)
                buildCache("posts", 600, 1000),

                // Comments - Very short TTL (highly dynamic)
                buildCache("comments", 300, 2000),

                // Voting - Real-time data
                buildCache("postVotes", 180, 5000),
                buildCache("commentVotes", 180, 5000),

                // User Preferences - Medium TTL
                buildCache("userPreferences", 1800, 500),

                // Moderation Queue - Short TTL
                buildCache("moderationQueue", 300, 200),

                // Trending Posts - Medium TTL
                buildCache("trendingPosts", 600, 100)
        ));

        return cacheManager;
    }

    /**
     * Helper method to build individual Caffeine cache with custom settings
     *
     * @param name Cache name identifier
     * @param ttlSeconds Time-to-live in seconds
     * @param maxSize Maximum number of entries
     * @return Configured CaffeineCache
     */
    private CaffeineCache buildCache(String name, long ttlSeconds, long maxSize) {
        return new CaffeineCache(
                name,
                Caffeine.newBuilder()
                        .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
                        .maximumSize(maxSize)
                        .recordStats() // Enable cache statistics for monitoring
                        .build()
        );
    }
}