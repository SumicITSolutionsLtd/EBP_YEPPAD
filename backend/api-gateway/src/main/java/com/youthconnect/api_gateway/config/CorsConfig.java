package com.youthconnect.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Global CORS Configuration for API Gateway
 *
 * Handles Cross-Origin Resource Sharing for all incoming requests.
 * Supports multiple environments (development, staging, production).
 *
 * This configuration is critical for allowing frontend applications
 * from different domains to access the API Gateway.
 *
 * Location: api-gateway/src/main/java/com/youthconnect/api_gateway/config/
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
    private String allowedOriginsString;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethodsString;

    @Value("${app.cors.max-age:3600}")
    private Long maxAge;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Parse allowed origins from comma-separated string
        List<String> allowedOrigins = Arrays.asList(allowedOriginsString.split(","));
        corsConfig.setAllowedOrigins(allowedOrigins);

        // Parse allowed methods from comma-separated string
        List<String> allowedMethods = Arrays.asList(allowedMethodsString.split(","));
        corsConfig.setAllowedMethods(allowedMethods);

        // Allow all headers (including Authorization, Content-Type, etc.)
        corsConfig.addAllowedHeader("*");

        // Allow credentials (cookies, authorization headers, TLS client certificates)
        corsConfig.setAllowCredentials(true);

        // Preflight request cache duration (1 hour)
        corsConfig.setMaxAge(maxAge);

        // Expose headers that client can access
        corsConfig.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-Rate-Limit-Remaining",
                "X-Rate-Limit-Retry-After-Seconds"
        ));

        // Apply CORS configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}