package com.youthconnect.ai.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async Configuration for AI Recommendation Service
 * Enables asynchronous processing for performance optimization
 *
 * Used for:
 * - Activity logging (non-blocking)
 * - Background ML model training
 * - Batch recommendation generation
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Task executor for general async operations
     * Optimized for activity logging and lightweight tasks
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AI-Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * Dedicated executor for ML model operations
     * Higher resource allocation for compute-intensive tasks
     */
    @Bean(name = "mlModelExecutor")
    public Executor mlModelExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("ML-Model-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();
        return executor;
    }

    /**
     * Executor for recommendation generation
     * Balanced configuration for batch processing
     */
    @Bean(name = "recommendationExecutor")
    public Executor recommendationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(12);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("Recommendation-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(90);
        executor.initialize();
        return executor;
    }
}