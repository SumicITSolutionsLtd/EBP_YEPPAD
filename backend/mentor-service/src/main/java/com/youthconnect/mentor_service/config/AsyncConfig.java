package com.youthconnect.mentor_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

/**
 * ============================================================================
 * ASYNC CONFIGURATION
 * ============================================================================
 *
 * Configures asynchronous task execution with multiple specialized thread pools.
 * Enables non-blocking operations for notifications, reminders, and analytics.
 *
 * THREAD POOLS:
 * 1. taskExecutor - General async operations
 * 2. notificationExecutor - Notification delivery
 * 3. reminderExecutor - Session reminders
 * 4. analyticsExecutor - Analytics processing
 * 5. taskScheduler - Scheduled jobs (reminders, cleanup)
 *
 * BENEFITS:
 * - Non-blocking I/O operations
 * - Better resource utilization
 * - Improved response times
 * - Isolated failure domains
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-21
 * ============================================================================
 */
@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    /**
     * General purpose async executor
     * Used for miscellaneous async operations
     *
     * CONFIGURATION:
     * - Core pool: 5 threads
     * - Max pool: 10 threads
     * - Queue capacity: 100 tasks
     * - Graceful shutdown with 60s wait
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("Initialized general async executor with core pool size: {}",
                executor.getCorePoolSize());
        return executor;
    }

    /**
     * Notification-specific executor
     * Handles all notification delivery tasks
     *
     * RATIONALE:
     * - Isolated from general tasks
     * - Higher priority for user communication
     * - Smaller pool due to I/O-bound nature
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("notification-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("Initialized notification executor with core pool size: {}",
                executor.getCorePoolSize());
        return executor;
    }

    /**
     * Reminder-specific executor
     * Handles session reminder processing
     *
     * RATIONALE:
     * - Time-sensitive operations
     * - Batch processing of reminders
     * - Dedicated resources for reliability
     */
    @Bean(name = "reminderExecutor")
    public Executor reminderExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("reminder-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("Initialized reminder executor with core pool size: {}",
                executor.getCorePoolSize());
        return executor;
    }

    /**
     * Analytics-specific executor
     * Handles analytics and reporting tasks
     *
     * RATIONALE:
     * - CPU-intensive operations
     * - Lower priority than user-facing tasks
     * - Can tolerate delays
     */
    @Bean(name = "analyticsExecutor")
    public Executor analyticsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("analytics-");
        executor.setWaitForTasksToCompleteOnShutdown(false); // Can interrupt
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        log.info("Initialized analytics executor with core pool size: {}",
                executor.getCorePoolSize());
        return executor;
    }

    /**
     * Task scheduler for scheduled jobs
     * Used by @Scheduled annotated methods
     *
     * USAGE:
     * - Session reminder checks (every minute)
     * - Cleanup jobs (daily)
     * - Statistics updates (hourly)
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("scheduled-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.initialize();

        log.info("Initialized task scheduler with pool size: {}",
                scheduler.getPoolSize());
        return scheduler;
    }

    /**
     * Exception handler for async method failures
     * Logs uncaught exceptions from @Async methods
     *
     * IMPORTANT:
     * - Exceptions in @Async methods are silent by default
     * - This handler ensures they're logged
     * - Integrates with monitoring systems
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(Throwable throwable, Method method, Object... params) {
                log.error("Uncaught async exception in method: {}.{}",
                        method.getDeclaringClass().getSimpleName(),
                        method.getName(),
                        throwable);
                log.error("Method parameters: {}", (Object) params);

                // TODO: Send alert to monitoring system for critical failures
            }
        };
    }
}