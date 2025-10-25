package com.youthconnect.opportunity_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

/**
 * Asynchronous processing configuration for background tasks.
 * Enables @Async annotation support and defines thread pools for different task types.
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * Main thread pool for general asynchronous operations
     * Used for: Event publishing, notifications, logging
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(15);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("OpportunityAsync-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("Initialized general async task executor with core pool size: {}",
                executor.getCorePoolSize());
        return executor;
    }

    /**
     * Dedicated thread pool for notification sending
     * Isolated to prevent notification delays from blocking other operations
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("NotificationAsync-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();

        log.info("Initialized notification async executor with core pool size: {}",
                executor.getCorePoolSize());
        return executor;
    }

    /**
     * Thread pool for analytics and reporting tasks
     * Lower priority, larger queue to handle batch operations
     */
    @Bean(name = "analyticsExecutor")
    public Executor analyticsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("AnalyticsAsync-");
        executor.setWaitForTasksToCompleteOnShutdown(false); // Can be terminated
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        log.info("Initialized analytics async executor with core pool size: {}",
                executor.getCorePoolSize());
        return executor;
    }
}