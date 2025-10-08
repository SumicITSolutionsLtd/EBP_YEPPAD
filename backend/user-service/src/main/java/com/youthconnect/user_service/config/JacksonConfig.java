// JacksonConfig.java
package com.youthconnect.user_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Jackson JSON Configuration
 *
 * Configures JSON serialization/deserialization settings for consistent
 * API responses and proper handling of Java 8 time types.
 */
@Slf4j
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        log.info("Configuring Jackson ObjectMapper");

        ObjectMapper mapper = new ObjectMapper();

        // Register JavaTimeModule for LocalDateTime, LocalDate, etc.
        mapper.registerModule(new JavaTimeModule());

        // Don't serialize dates as timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Use snake_case for JSON properties (optional - depends on frontend preference)
        // mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        // Handle unknown properties gracefully
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Don't fail on empty beans
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // Pretty print JSON in development
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        return mapper;
    }
}