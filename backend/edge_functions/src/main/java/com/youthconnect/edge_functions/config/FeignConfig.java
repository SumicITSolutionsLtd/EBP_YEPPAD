package com.youthconnect.edge_functions.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign Client Configuration for Edge Functions Service
 *
 * Configures:
 * - Request/response timeouts
 * - Retry policies with exponential backoff
 * - Error handling and logging
 * - Connection pooling
 *
 * @author Douglas Kings Kato
 * @version 1.0
 */
@Configuration
@Slf4j
public class FeignConfig {

    /**
     * Feign request options with timeouts
     *
     * Connection timeout: 5 seconds
     * Read timeout: 10 seconds
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                5, TimeUnit.SECONDS,  // Connection timeout
                10, TimeUnit.SECONDS, // Read timeout
                true                  // Follow redirects
        );
    }

    /**
     * Retry configuration with exponential backoff
     *
     * Max attempts: 3
     * Period: 1 second
     * Max period: 5 seconds
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
                1000,   // Period (1 second)
                5000,   // Max period (5 seconds)
                3       // Max attempts
        );
    }

    /**
     * Feign logging level
     * BASIC: Request method/URL, response status
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Custom error decoder for Feign clients
     *
     * Handles HTTP error responses from downstream services
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            String serviceName = methodKey.split("#")[0];
            int status = response.status();

            log.error("Feign client error: {} - Status: {}", serviceName, status);

            switch (status) {
                case 400:
                    return new IllegalArgumentException(
                            "Bad request to " + serviceName
                    );
                case 401:
                    return new SecurityException(
                            "Unauthorized access to " + serviceName
                    );
                case 403:
                    return new SecurityException(
                            "Forbidden: " + serviceName
                    );
                case 404:
                    return new IllegalArgumentException(
                            "Resource not found in " + serviceName
                    );
                case 429:
                    return new RuntimeException(
                            "Rate limit exceeded for " + serviceName
                    );
                case 503:
                    return new RuntimeException(
                            serviceName + " temporarily unavailable"
                    );
                default:
                    if (status >= 500) {
                        return new RuntimeException(
                                serviceName + " internal error: " + status
                        );
                    }
                    return new RuntimeException(
                            "Unexpected error from " + serviceName
                    );
            }
        };
    }
}