package com.youthconnect.job_services.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

/**
 * Cache Configuration
 *
 * Configures Caffeine in-memory caching for improved performance.
 * Reduces database load for frequently accessed data.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Cache Manager with multiple cache regions
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(defaultCacheBuilder());

        // Define cache names
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "jobs",           // Job details
                "categories",     // Job categories
                "jobsByCategory", // Jobs filtered by category
                "activeJobs",     // Active published jobs
                "applications",   // Application details
                "userApplications" // User's application list
        ));

        return cacheManager;
    }

    /**
     * Default cache configuration
     */
    private Caffeine<Object, Object> defaultCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)  // Cache expires after 30 min
                .maximumSize(10000)                      // Max 10,000 entries per cache
                .recordStats();                          // Enable statistics
    }
}