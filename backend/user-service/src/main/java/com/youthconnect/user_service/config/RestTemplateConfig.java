package com.youthconnect.user_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate Configuration for Youth Connect Uganda User Service
 *
 * Configures RestTemplate beans for HTTP client communication with other microservices.
 * This configuration is essential for the NotificationService to communicate with
 * external services and other microservices in the ecosystem.
 *
 * Key Features:
 * - Load-balanced RestTemplate for Eureka service discovery
 * - Connection timeout and read timeout configuration
 * - Request/response buffering for logging and retries
 * - Error handling and resilience
 *
 * Usage:
 * - NotificationService uses this to call external SMS/Email APIs
 * - Feign clients can fall back to RestTemplate for non-declarative calls
 * - Internal microservice communication via Eureka service names
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2024-11-21
 */
@Slf4j
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a load-balanced RestTemplate for microservice communication.
     *
     * The @LoadBalanced annotation enables client-side load balancing through
     * Spring Cloud LoadBalancer. This allows the RestTemplate to resolve service
     * names (e.g., "notification-service") to actual IP addresses via Eureka.
     *
     * Configuration Details:
     * - Connection Timeout: 30 seconds - Time to establish connection
     * - Read Timeout: 60 seconds - Time to wait for response data
     * - Buffering: Enabled - Allows request/response body to be read multiple times
     *
     * Example Usage in NotificationService:
     * <pre>
     * {@code
     * String url = "http://notification-service/api/sms/send";
     * ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
     * }
     * </pre>
     *
     * @param builder RestTemplateBuilder provided by Spring Boot auto-configuration
     * @return Configured RestTemplate with load balancing capabilities
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        log.info("Configuring load-balanced RestTemplate for microservice communication");

        RestTemplate restTemplate = builder
                // Connection timeout - how long to wait when establishing connection
                .setConnectTimeout(Duration.ofSeconds(30))

                // Read timeout - how long to wait for data after connection established
                .setReadTimeout(Duration.ofSeconds(60))

                // Build the RestTemplate instance
                .build();

        // Enable request/response buffering for better error handling and logging
        // This allows interceptors to read the request/response body multiple times
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(
                new SimpleClientHttpRequestFactory()
        );
        restTemplate.setRequestFactory(factory);

        log.info("RestTemplate configured successfully with load balancing enabled");
        log.debug("RestTemplate settings - Connection timeout: 30s, Read timeout: 60s");

        return restTemplate;
    }

    /**
     * Creates a standard RestTemplate without load balancing.
     *
     * This bean is used for external API calls that don't go through Eureka
     * service discovery (e.g., Africa's Talking SMS API, external email services).
     *
     * Use Cases:
     * - Calling third-party APIs with direct URLs
     * - External service integrations (SMS, Email, Payment gateways)
     * - APIs that don't require service discovery
     *
     * Example Usage:
     * <pre>
     * {@code
     * @Qualifier("externalRestTemplate")
     * private final RestTemplate restTemplate;
     *
     * String url = "https://api.africastalking.com/version1/messaging";
     * ResponseEntity<String> response = restTemplate.postForEntity(url, smsPayload, String.class);
     * }
     * </pre>
     *
     * @param builder RestTemplateBuilder provided by Spring Boot
     * @return Configured RestTemplate for external API communication
     */
    @Bean("externalRestTemplate")
    public RestTemplate externalRestTemplate(RestTemplateBuilder builder) {
        log.info("Configuring standard RestTemplate for external API communication");

        RestTemplate restTemplate = builder
                // Longer timeout for external APIs (they may be slower)
                .setConnectTimeout(Duration.ofSeconds(45))
                .setReadTimeout(Duration.ofSeconds(90))
                .build();

        // Enable buffering for external APIs as well
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(
                new SimpleClientHttpRequestFactory()
        );
        restTemplate.setRequestFactory(factory);

        log.info("External RestTemplate configured successfully");
        log.debug("External RestTemplate settings - Connection timeout: 45s, Read timeout: 90s");

        return restTemplate;
    }

    /**
     * Provides custom ClientHttpRequestFactory with advanced configuration.
     *
     * This factory configures low-level HTTP client settings for optimal
     * performance and reliability in production environments.
     *
     * Advanced Settings:
     * - Connection timeout: 30 seconds
     * - Read timeout: 60 seconds
     * - Buffering: Enabled for retry capabilities
     * - Connection pooling: Handled by underlying HTTP client
     *
     * @return Configured ClientHttpRequestFactory
     */
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        log.debug("Configuring ClientHttpRequestFactory with custom settings");

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // Connection timeout - time to establish TCP connection
        factory.setConnectTimeout(30000); // 30 seconds in milliseconds

        // Read timeout - time to wait for data after connection established
        factory.setReadTimeout(60000); // 60 seconds in milliseconds

        // Enable buffering to allow reading request/response bodies multiple times
        // This is crucial for retry mechanisms and error logging
        BufferingClientHttpRequestFactory bufferingFactory =
                new BufferingClientHttpRequestFactory(factory);

        log.debug("ClientHttpRequestFactory configured with buffering enabled");

        return bufferingFactory;
    }

    /**
     * Bean post-processor that logs RestTemplate creation.
     *
     * This method demonstrates that the RestTemplate beans are successfully
     * created and available in the Spring application context. It's particularly
     * useful for debugging startup issues.
     *
     * Note: This is invoked automatically by Spring after beans are created.
     */
    @Bean
    public RestTemplateLogger restTemplateLogger() {
        return new RestTemplateLogger();
    }

    /**
     * Simple logger class to confirm RestTemplate bean creation.
     */
    static class RestTemplateLogger {
        public RestTemplateLogger() {
            log.info("✅ RestTemplate beans created successfully");
            log.info("   - Load-balanced RestTemplate: Available for microservice calls");
            log.info("   - External RestTemplate: Available for third-party API calls");
            log.info("   - NotificationService can now use RestTemplate for SMS/Email APIs");
        }
    }
}
