package com.youthconnect.file.service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async processing configuration for file operations
 * Handles concurrent file uploads and processing
 */
@Slf4j
@Configuration
public class AsyncConfiguration implements AsyncConfigurer {

    @Bean(name = "fileTaskExecutor")
    public Executor fileTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("file-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("File Task Executor initialized with core pool size: {}", executor.getCorePoolSize());
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return fileTaskExecutor();
    }
}