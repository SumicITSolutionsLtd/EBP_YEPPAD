package com.youthconnect.file.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT Configuration Properties
 *
 * Binds to jwt.* properties in application.yml
 * Used for validating JWT tokens from API Gateway
 *
 * @author Douglas Kings Kato
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * Secret key for JWT validation (must match API Gateway)
     */
    private String secret = "youth-connect-secure-secret-key-2025-minimum-256-bits-required-for-production";

    /**
     * Token expiration time in milliseconds (24 hours)
     */
    private Long expiration = 86400000L;

    /**
     * Token prefix in Authorization header (e.g., "Bearer ")
     */
    private String tokenPrefix = "Bearer ";

    /**
     * Header name containing JWT token
     */
    private String headerString = "Authorization";
}