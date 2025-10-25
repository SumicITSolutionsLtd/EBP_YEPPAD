package com.youthconnect.ussd_service.config;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration; // ✓ FIXED: Import Duration for timeout
import java.util.concurrent.TimeUnit;

/**
 * Feign Client Configuration for Service-to-Service Communication.
 *
 * <p>Configures:
 * <ul>
 *   <li>Connection and read timeouts</li>
 *   <li>Retry policy with exponential backoff</li>
 *   <li>Custom error decoder</li>
 *   <li>Request interceptors for headers</li>
 *   <li>Logging levels</li>
 * </ul>
 *
 * @author Douglas Kings Kato & Harold
 * @version 2.0.0
 */
@Slf4j
@Configuration
public class FeignConfig {

    /**
     * Feign logging level configuration.
     * FULL for development, BASIC for production.
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * Custom request options with timeout configuration.
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                5, TimeUnit.SECONDS,  // Connect timeout
                30, TimeUnit.SECONDS, // Read timeout
                true                  // Follow redirects
        );
    }

    /**
     * Custom retry policy for transient failures.
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
                1000,  // Initial retry interval (ms)
                3000,  // Max retry interval (ms)
                3      // Max retry attempts
        );
    }

    /**
     * Request interceptor to add custom headers.
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Request-Source", "USSD-Service");
            requestTemplate.header("X-Client-Version", "1.0.0");

            String requestId = java.util.UUID.randomUUID().toString();
            requestTemplate.header("X-Request-ID", requestId);
            requestTemplate.header("X-Request-Timestamp",
                    String.valueOf(System.currentTimeMillis()));

            log.debug("Feign Request: {} {} [Request-ID: {}]",
                    requestTemplate.method(),
                    requestTemplate.url(),
                    requestId);
        };
    }

    /**
     * ✓ FIXED: Custom error decoder with correct RetryableException constructor.
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            String requestUrl = response.request().url();
            int status = response.status();

            log.error("Feign Client Error: {} returned status {} for URL: {}",
                    methodKey, status, requestUrl);

            switch (status) {
                case 400:
                    return new IllegalArgumentException(
                            "Bad request to " + methodKey + ": Invalid parameters");

                case 401:
                case 403:
                    return new SecurityException(
                            "Authentication failed for " + methodKey);

                case 404:
                    return new RuntimeException(
                            "Resource not found: " + requestUrl);

                case 409:
                    return new RuntimeException(
                            "Data conflict: Resource already exists");

                case 429:
                    // ✓ FIXED: Correct RetryableException constructor
                    return new RetryableException(
                            status,
                            "Rate limit exceeded for " + methodKey,
                            response.request().httpMethod(),
                            // ✓ FIXED: Use Duration instead of Date for retry after
                            Duration.ofSeconds(30), // Retry after 30 seconds
                            response.request()
                    );

                case 500:
                case 503:
                    // ✓ FIXED: Correct RetryableException constructor
                    return new RetryableException(
                            status,
                            "Service unavailable: " + methodKey,
                            response.request().httpMethod(),
                            Duration.ofSeconds(10), // Retry after 10 seconds
                            response.request()
                    );

                default:
                    return new RuntimeException(
                            "Unexpected error (status " + status + ") for " + methodKey);
            }
        };
    }
}