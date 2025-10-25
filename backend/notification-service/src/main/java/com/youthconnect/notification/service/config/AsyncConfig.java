package com.youthconnect.notification.service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * ASYNC CONFIGURATION - THREAD POOL MANAGEMENT
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Configures custom thread pool for asynchronous notification delivery.
 *
 * Thread Pool Sizing Strategy:
 * - Core Pool: 10 threads (always alive)
 * - Max Pool: 50 threads (scales under load)
 * - Queue Capacity: 1000 pending tasks
 * - Keep Alive: 60 seconds for idle threads
 *
 * This configuration ensures:
 * ✓ Non-blocking notification delivery
 * ✓ Efficient resource utilization
 * ✓ Graceful handling of traffic spikes
 * ✓ Proper thread naming for debugging
 *
 * @author Douglas Kings Kato
 * @version 1.0
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * Primary thread pool executor for notification tasks.
     *
     * Rejection Policy: CallerRuns
     * - If all threads are busy and queue is full
     * - Task executes in calling thread (backpressure)
     * - Prevents notification loss under extreme load
     */
    @Bean(name = "notificationTaskExecutor")
    public Executor notificationTaskExecutor() {

        log.info("🔧 Initializing Notification Task Executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Thread Pool Configuration
        executor.setCorePoolSize(10);           // Minimum threads always alive
        executor.setMaxPoolSize(50);            // Maximum concurrent threads
        executor.setQueueCapacity(1000);        // Pending task queue size
        executor.setKeepAliveSeconds(60);       // Idle thread timeout

        // Thread Naming (for debugging and monitoring)
        executor.setThreadNamePrefix("notification-async-");

        // Rejection Policy: Execute in caller thread if pool is saturated
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Wait for tasks to complete on shutdown (graceful shutdown)
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();

        log.info("✅ Notification Task Executor initialized: core={}, max={}, queue={}",
                executor.getCorePoolSize(),
                executor.getMaxPoolSize(),
                executor.getQueueCapacity());

        return executor;
    }

    /**
     * Executor specifically for scheduled tasks (retry mechanism).
     * Smaller pool size since retries are less frequent.
     */
    @Bean(name = "retryTaskExecutor")
    public Executor retryTaskExecutor() {

        log.info("🔧 Initializing Retry Task Executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("notification-retry-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        log.info("✅ Retry Task Executor initialized");

        return executor;
    }
}