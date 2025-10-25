package com.youthconnect.user_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for User Service
 *
 * KEY PRINCIPLE: This service does NOT handle authentication!
 * - All JWT validation is handled by auth-service + API Gateway
 * - This config only defines which endpoints are accessible
 *
 * Security Model:
 * 1. Internal endpoints (/api/v1/users/internal/**)
 *    - Should only be accessed within the service mesh
 *    - API Gateway must BLOCK external access
 *
 * 2. Public endpoints
 *    - Health checks, actuator info, and API docs are open
 *
 * 3. Protected endpoints
 *    - All other endpoints require authentication
 *    - JWT is validated upstream by API Gateway
 *
 * Notes:
 * - Sessions are stateless (no cookies)
 * - CSRF and CORS are disabled (handled externally)
 *
 * @author Douglas Kings Kato
 * @version 1.1.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF - not needed for stateless REST API
                .csrf(AbstractHttpConfigurer::disable)

                // Disable CORS - handled by API Gateway
                .cors(AbstractHttpConfigurer::disable)

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Internal service-to-service endpoints
                        .requestMatchers("/api/v1/users/internal/**").permitAll()

                        // Health and monitoring
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()

                        // API documentation
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // All other requests require authentication (validated upstream)
                        .anyRequest().authenticated()
                )

                // Stateless session - no session cookies
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}
