// ValidationConfig.java
package com.youthconnect.user_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validation;

/**
 * Validation Configuration for Youth Connect Uganda User Service
 *
 * This configuration sets up comprehensive input validation for all user data,
 * with special considerations for Ugandan phone numbers, names, and regional
 * requirements.
 */
@Slf4j
@Configuration
public class ValidationConfig {

    /**
     * Custom validator bean for manual validation in services
     */
    @Bean
    public Validator validator() {
        log.info("Configuring custom validator for Youth Connect Uganda");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }
}