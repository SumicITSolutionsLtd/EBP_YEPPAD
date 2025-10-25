package com.youthconnect.content_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration
 * Handles CORS, interceptors, etc.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * CORS configuration for development
     * In production, this should be handled by API Gateway
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:3000",  // React dev
                        "http://localhost:3001",  // Mobile dev
                        "http://localhost:4200"   // Angular dev (if used)
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}