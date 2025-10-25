package com.youthconnect.api_gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Gateway Configuration
 *
 * Provides additional beans needed by the API Gateway:
 * - ObjectMapper for JSON serialization
 * - WebClient for making HTTP requests to backend services
 *
 * Location: api-gateway/src/main/java/com/youthconnect/api_gateway/config/
 */
@Configuration
public class GatewayConfig {

    /**
     * ObjectMapper bean for JSON serialization/deserialization
     * Used by GlobalExceptionHandler and other components
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Support for Java 8 date/time types (LocalDateTime, etc.)
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * WebClient with load balancing for calling backend services
     * The @LoadBalanced annotation enables Eureka service discovery
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}