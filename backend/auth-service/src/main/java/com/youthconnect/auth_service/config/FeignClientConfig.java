package com.youthconnect.auth_service.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign Client Configuration
 *
 * Configures Feign clients for inter-service communication with:
 * <ul>
 *     <li>Connection timeouts</li>
 *     <li>Read timeouts</li>
 *     <li>Retry policies</li>
 *     <li>Logging levels</li>
 * </ul>
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
 */
@Configuration
public class FeignClientConfig {

    /**
     * Configure Feign Logger Level
     *
     * BASIC: Logs only request method, URL, response status, and execution time
     *
     * @return Logger.Level for Feign clients
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Configure Request Options
     *
     * Sets connection and read timeout values for all Feign clients.
     *
     * Timeouts:
     * - Connect: 5 seconds (time to establish connection)
     * - Read: 10 seconds (time to receive response after connection)
     *
     * @return Request.Options with timeout configuration
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                5000,  // connectTimeout in milliseconds
                TimeUnit.MILLISECONDS,
                10000, // readTimeout in milliseconds
                TimeUnit.MILLISECONDS,
                true   // followRedirects
        );
    }

    /**
     * Configure Retry Policy
     *
     * Retries failed requests with exponential backoff.
     *
     * Policy:
     * - Max attempts: 3
     * - Initial interval: 100ms
     * - Max interval: 1000ms
     * - Multiplier: 2 (doubles each time)
     *
     * @return Retryer with exponential backoff
     */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
                100,   // period (initial interval)
                1000,  // maxPeriod (max interval)
                3      // maxAttempts
        );
    }
}