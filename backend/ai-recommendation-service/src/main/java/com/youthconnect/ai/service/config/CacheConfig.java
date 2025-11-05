package com.youthconnect.ai.service.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache Configuration for AI Recommendation Service
 * Uses Caffeine for high-performance in-memory caching
 *
 * Cache Regions:
 * - opportunityRecommendations: 1 hour TTL
 * - contentRecommendations: 1 hour TTL
 * - mentorRecommendations: 30 minutes TTL
 * - userBehavior: 15 minutes TTL
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure Caffeine cache manager with optimized settings
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "opportunityRecommendations",
                "contentRecommendations",
                "mentorRecommendations",
                "userBehavior"
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000) // Max 1000 entries per cache
                .expireAfterWrite(1, TimeUnit.HOURS) // Default TTL: 1 hour
                .recordStats() // Enable metrics
        );

        return cacheManager;
    }
}