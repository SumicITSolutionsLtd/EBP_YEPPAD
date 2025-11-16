package com.youthconnect.file.service.config;

import com.youthconnect.file.service.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security Configuration for File Management Service
 *
 * GUIDELINES COMPLIANCE:
 * ✅ Implements JWT authentication through API Gateway
 * ✅ Service remains stateless
 * ✅ Public health check endpoint available
 * ✅ Follows centralized authentication pattern
 *
 * ARCHITECTURE:
 * - API Gateway validates JWT tokens
 * - Gateway adds X-User-Id header with authenticated user ID
 * - Service trusts Gateway's authentication (internal network only)
 * - Public endpoints for health checks and public file downloads
 *
 * Configures:
 * - JWT authentication via filter
 * - Public vs. protected endpoints
 * - CORS settings for file uploads
 * - Stateless session management
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (With Authentication Guidelines)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize annotations
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configure HTTP security
     *
     * PUBLIC ENDPOINTS (No authentication required):
     * - /api/files/health (health check - REQUIRED by guidelines)
     * - /api/files/download/public/** (public file downloads)
     * - /api/files/download/modules/** (public learning modules)
     * - /actuator/health (monitoring)
     * - /swagger-ui/** (API documentation)
     * - /v3/api-docs/** (OpenAPI docs)
     *
     * PROTECTED ENDPOINTS (JWT required):
     * - All file upload operations
     * - Private file downloads
     * - File deletion
     * - User-specific file listings
     *
     * SECURITY FEATURES:
     * - CSRF disabled (stateless API with JWT)
     * - CORS configured for file uploads
     * - Stateless session management
     * - JWT filter before Spring Security filter
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (stateless API with JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // ✅ PUBLIC ENDPOINTS - Required by guidelines
                        .requestMatchers("/api/files/health").permitAll()
                        .requestMatchers("/api/files/download/public/**").permitAll()
                        .requestMatchers("/api/files/download/modules/**").permitAll()

                        // Actuator endpoints (monitoring)
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Swagger/OpenAPI documentation
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/api-docs/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        // ✅ PROTECTED ENDPOINTS - All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Stateless session management (no server-side sessions)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Add JWT authentication filter before standard authentication
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configure CORS for file uploads
     *
     * FEATURES:
     * - Multiple origins (dev, staging, production)
     * - File upload via multipart/form-data
     * - Credentials (cookies, authorization headers)
     * - File download headers exposed
     *
     * ALLOWED ORIGINS:
     * - http://localhost:3000 (Web dev)
     * - http://localhost:3001 (Admin dev)
     * - http://localhost:19006 (React Native dev)
     * - https://youthconnect.ug (Production)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",     // Web frontend (dev)
                "http://localhost:3001",     // Admin dashboard (dev)
                "http://localhost:19006",    // React Native (dev)
                "https://youthconnect.ug",   // Production web
                "https://www.youthconnect.ug" // Production web (www)
        ));

        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        // Allowed headers (including file upload headers)
        configuration.setAllowedHeaders(List.of("*"));

        // Expose headers for file download
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Disposition",
                "X-File-Name",
                "X-File-Size",
                "X-Total-Count"
        ));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Max age for preflight requests (1 hour)
        configuration.setMaxAge(3600L);

        // Apply to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}