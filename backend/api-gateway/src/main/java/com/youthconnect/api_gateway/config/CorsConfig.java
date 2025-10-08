package com.youthconnect.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.Collections;

/**
 * Global CORS Configuration for the API Gateway.
 *
 * This is the definitive, "fix it once and for all" solution for CORS issues.
 * It programmatically creates a Spring Bean that intercepts all incoming requests at the earliest stage
 * and applies the correct CORS headers to the response, including for the preflight OPTIONS request.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        // 1. Create a new CorsConfiguration object where we will define our rules.
        CorsConfiguration corsConfig = new CorsConfiguration();

        // 2. Specify the allowed origins. We explicitly trust requests from our React dev server.
        corsConfig.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));

        // 3. Specify the allowed HTTP headers. "*" allows all headers needed ('Content-Type', 'Authorization', etc.).
        corsConfig.setAllowedHeaders(Collections.singletonList("*"));

        // 4. Specify the allowed HTTP methods. It's crucial to include "OPTIONS" for the preflight request to succeed.
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 5. Allow credentials (like cookies or tokens in headers) to be sent with the request.
        corsConfig.setAllowCredentials(true);

        // 6. Create a source for our CORS configuration.
        // We apply the 'corsConfig' rules to ALL incoming paths ("/**").
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        // 7. Return a new CorsWebFilter. This filter will be applied to every request.
        return new CorsWebFilter(source);
    }
}