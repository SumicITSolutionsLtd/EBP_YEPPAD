package com.youthconnect.content_service.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign Client Configuration
 * For inter-service communication
 */
@Configuration
public class FeignClientConfig {

    /**
     * Feign logging level
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC; // NONE, BASIC, HEADERS, FULL
    }

    /**
     * Request interceptor to add headers to all Feign requests
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Content-Type", "application/json");
            requestTemplate.header("Accept", "application/json");
            // Add JWT token from security context in production
        };
    }
}