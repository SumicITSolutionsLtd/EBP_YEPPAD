package com.youthconnect.opportunity_service.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Configuration for Feign HTTP clients used for inter-service communication.
 * Enables communication with User Service, Notification Service, and Analytics Service.
 */
@Configuration
@EnableFeignClients(basePackages = "com.youthconnect.opportunity_service.client")
@Slf4j
public class FeignConfig {

    /**
     * Request interceptor to add common headers to all Feign requests
     * Including: service identification, request tracing
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Service-Name", "opportunity-service");
            requestTemplate.header("X-Request-Source", "internal");
            // Add correlation ID for request tracing across services
            String correlationId = java.util.UUID.randomUUID().toString();
            requestTemplate.header("X-Correlation-ID", correlationId);

            log.debug("Added Feign request headers with correlation ID: {}", correlationId);
        };
    }

    /**
     * Custom error decoder for better error handling from downstream services
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            log.error("Feign client error - Method: {}, Status: {}, Reason: {}",
                    methodKey, response.status(), response.reason());

            // Handle specific HTTP status codes
            return switch (response.status()) {
                case 404 -> new RuntimeException("Resource not found in downstream service");
                case 503 -> new RuntimeException("Downstream service unavailable");
                default -> new RuntimeException("Error calling downstream service");
            };
        };
    }

    /**
     * Feign logging level for debugging
     * FULL logs request and response with headers and body
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}