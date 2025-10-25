package com.youthconnect.opportunity_service.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine for high-performance in-memory caching.
 * Reduces database load for frequently accessed data like opportunity listings.
 */
@Configuration
@EnableCaching
@RequiredArgsConstructor
@Slf4j
public class CacheConfig {

    private final ApplicationProperties properties;

    /**
     * Cache names used throughout the application
     */
    public static final String OPPORTUNITIES_CACHE = "opportunities";
    public static final String OPPORTUNITY_DETAIL_CACHE = "opportunityDetail";
    public static final String USER_APPLICATIONS_CACHE = "userApplications";
    public static final String STATISTICS_CACHE = "statistics";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                OPPORTUNITIES_CACHE,
                OPPORTUNITY_DETAIL_CACHE,
                USER_APPLICATIONS_CACHE,
                STATISTICS_CACHE
        );

        // Default cache configuration
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(properties.getCache().getMaxCacheSize())
                .expireAfterWrite(properties.getCache().getOpportunityListTtl(), TimeUnit.SECONDS)
                .recordStats());

        log.info("Initialized Caffeine cache manager with {} cache regions", 4);
        return cacheManager;
    }

    /**
     * Specialized cache for opportunity details with longer TTL
     */
    @Bean
    public Caffeine<Object, Object> opportunityDetailCaffeine() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(properties.getCache().getOpportunityDetailTtl(), TimeUnit.SECONDS)
                .recordStats();
    }
}