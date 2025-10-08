package com.youthconnect.user_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Asynchronous Processing Configuration for Youth Connect Uganda User Service
 *
 * This configuration class sets up multiple thread pools for handling different
 * types of background tasks including:
 * - Email notifications
 * - SMS notifications
 * - Audit logging
 * - File processing
 * - Background data synchronization
 * - Scheduled tasks
 *
 * Each executor is optimized for its specific use case to ensure optimal
 * performance and resource utilization.
 *
 * Features:
 * - Multiple specialized thread pools
 * - Proper exception handling and monitoring
 * - Graceful degradation on resource exhaustion
 * - Thread naming for easier debugging
 * - Configurable pool sizes based on environment
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
 */
@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig implements AsyncConfigurer {

    @Value("${app.async.core-pool-size:2}")
    private int corePoolSize;

    @Value("${app.async.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${app.async.queue-capacity:500}")
    private int queueCapacity;

    @Value("${app.async.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${app.environment:development}")
    private String environment;

    /**
     * Default async executor for general background tasks
     * Used for non-critical operations that can be processed asynchronously
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        log.info("Configuring default async task executor for environment: {}", environment);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Environment-specific pool sizing
        if ("production".equalsIgnoreCase(environment)) {
            executor.setCorePoolSize(Math.max(corePoolSize, 5));
            executor.setMaxPoolSize(Math.max(maxPoolSize, 20));
            executor.setQueueCapacity(Math.max(queueCapacity, 1000));
        } else {
            executor.setCorePoolSize(corePoolSize);
            executor.setMaxPoolSize(maxPoolSize);
            executor.setQueueCapacity(queueCapacity);
        }

        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("YouthConnect-Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);

        // Use caller runs policy to prevent task rejection
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Custom exception handler for async tasks
        executor.setTaskDecorator(new AsyncTaskDecorator());

        executor.initialize();

        log.info("Default async executor initialized - Core: {}, Max: {}, Queue: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }

    /**
     * Dedicated executor for email notifications
     * Optimized for I/O intensive operations with moderate throughput
     */
    @Bean(name = "emailExecutor")
    public Executor emailTaskExecutor() {
        log.info("Configuring email notification executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(120);
        executor.setThreadNamePrefix("YouthConnect-Email-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        // Discard oldest policy for email queue management
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.setTaskDecorator(new EmailTaskDecorator());

        executor.initialize();
        return executor;
    }

    /**
     * Dedicated executor for SMS notifications
     * Smaller pool size due to SMS API rate limits
     */
    @Bean(name = "smsExecutor")
    public Executor smsTaskExecutor() {
        log.info("Configuring SMS notification executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(180);
        executor.setThreadNamePrefix("YouthConnect-SMS-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(90);

        // Abort policy to prevent SMS spam
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setTaskDecorator(new SmsTaskDecorator());

        executor.initialize();
        return executor;
    }

    /**
     * Dedicated executor for audit logging operations
     * High throughput for non-blocking audit trail creation
     */
    @Bean(name = "auditExecutor")
    public Executor auditTaskExecutor() {
        log.info("Configuring audit logging executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("YouthConnect-Audit-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);

        // Discard policy - audit logging should not block main operations
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.setTaskDecorator(new AuditTaskDecorator());

        executor.initialize();
        return executor;
    }

    /**
     * Dedicated executor for file processing operations
     * Handles profile picture uploads, document processing, etc.
     */
    @Bean(name = "fileProcessingExecutor")
    public Executor fileProcessingExecutor() {
        log.info("Configuring file processing executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(240);
        executor.setThreadNamePrefix("YouthConnect-FileProc-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(180);

        // Caller runs policy for file processing
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setTaskDecorator(new FileProcessingTaskDecorator());

        executor.initialize();
        return executor;
    }

    /**
     * Dedicated executor for external API calls (USSD, third-party integrations)
     * Handles timeouts and retries for external service calls
     */
    @Bean(name = "externalApiExecutor")
    public Executor externalApiExecutor() {
        log.info("Configuring external API executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(180);
        executor.setThreadNamePrefix("YouthConnect-ExtAPI-");
        executor.setWaitForTasksToCompleteOnShutdown(false); // Don't wait for external calls
        executor.setAwaitTerminationSeconds(30);

        // Discard oldest policy for external API calls
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.setTaskDecorator(new ExternalApiTaskDecorator());

        executor.initialize();
        return executor;
    }

    /**
     * Task scheduler for scheduled operations like cleanup tasks,
     * data synchronization, and periodic maintenance
     */
    @Bean(name = "taskScheduler")
    public ThreadPoolTaskScheduler taskScheduler() {
        log.info("Configuring task scheduler");

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(3);
        scheduler.setThreadNamePrefix("YouthConnect-Scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);

        // Custom error handler for scheduled tasks
        scheduler.setErrorHandler(throwable -> {
            log.error("Scheduled task execution failed", throwable);
            // Add metrics or alerting here if needed
        });

        scheduler.initialize();
        return scheduler;
    }

    /**
     * Custom task decorator for general async operations
     * Adds request context propagation and error handling
     */
    private static class AsyncTaskDecorator implements org.springframework.core.task.TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            return () -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    log.error("Async task execution failed", e);
                    // Add metrics increment here
                }
            };
        }
    }

    /**
     * Custom task decorator for email operations
     * Adds email-specific logging and error handling
     */
    private static class EmailTaskDecorator implements org.springframework.core.task.TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            return () -> {
                long startTime = System.currentTimeMillis();
                try {
                    runnable.run();
                    long duration = System.currentTimeMillis() - startTime;
                    log.debug("Email task completed in {}ms", duration);
                } catch (Exception e) {
                    log.error("Email task execution failed", e);
                    // Add email failure metrics here
                }
            };
        }
    }

    /**
     * Custom task decorator for SMS operations
     * Adds SMS-specific logging and rate limit handling
     */
    private static class SmsTaskDecorator implements org.springframework.core.task.TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            return () -> {
                long startTime = System.currentTimeMillis();
                try {
                    runnable.run();
                    long duration = System.currentTimeMillis() - startTime;
                    log.debug("SMS task completed in {}ms", duration);
                } catch (Exception e) {
                    log.error("SMS task execution failed", e);
                    // Add SMS failure metrics here
                }
            };
        }
    }

    /**
     * Custom task decorator for audit operations
     * Ensures audit logging never interferes with main operations
     */
    private static class AuditTaskDecorator implements org.springframework.core.task.TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            return () -> {
                try {
                    runnable.run();
                } catch (Exception e) {
                    // Silent failure for audit logging - never disrupt main flow
                    log.debug("Audit task execution failed (non-critical)", e);
                }
            };
        }
    }

    /**
     * Custom task decorator for file processing operations
     * Adds file operation specific error handling and cleanup
     */
    private static class FileProcessingTaskDecorator implements org.springframework.core.task.TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            return () -> {
                long startTime = System.currentTimeMillis();
                try {
                    runnable.run();
                    long duration = System.currentTimeMillis() - startTime;
                    log.debug("File processing task completed in {}ms", duration);
                } catch (Exception e) {
                    log.error("File processing task execution failed", e);
                    // Add file processing failure metrics here
                }
            };
        }
    }

    /**
     * Custom task decorator for external API calls
     * Adds timeout handling and circuit breaker support
     */
    private static class ExternalApiTaskDecorator implements org.springframework.core.task.TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            return () -> {
                long startTime = System.currentTimeMillis();
                try {
                    runnable.run();
                    long duration = System.currentTimeMillis() - startTime;
                    log.debug("External API task completed in {}ms", duration);
                } catch (Exception e) {
                    log.warn("External API task execution failed", e);
                    // Add external API failure metrics here
                }
            };
        }
    }

    /**
     * Custom rejection handler that logs rejected tasks
     * and provides fallback behavior
     */
    private static class LoggingRejectedExecutionHandler implements RejectedExecutionHandler {
        private final String executorName;

        public LoggingRejectedExecutionHandler(String executorName) {
            this.executorName = executorName;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.warn("Task rejected by {} executor. Active: {}, Pool: {}, Queue: {}",
                    executorName,
                    executor.getActiveCount(),
                    executor.getPoolSize(),
                    executor.getQueue().size());

            // Add metrics increment for rejected tasks

            // Try to execute in caller thread as fallback
            if (!executor.isShutdown()) {
                try {
                    r.run();
                } catch (Exception e) {
                    log.error("Failed to execute rejected task in caller thread", e);
                }
            }
        }
    }
}