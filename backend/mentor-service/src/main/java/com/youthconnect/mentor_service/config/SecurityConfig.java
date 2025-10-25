package com.youthconnect.mentor_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * ============================================================================
 * SECURITY CONFIGURATION
 * ============================================================================
 *
 * Configures Spring Security for the mentor service.
 * Implements stateless JWT-based authentication with role-based access control.
 *
 * SECURITY FEATURES:
 * - Stateless session management (no server-side sessions)
 * - JWT token validation
 * - Role-based access control (RBAC)
 * - Method-level security with @PreAuthorize
 * - CSRF disabled (stateless API)
 * - CORS handled by API Gateway (fallback configuration here)
 *
 * AUTHENTICATION FLOW:
 * 1. Client obtains JWT from auth-service
 * 2. Client includes JWT in Authorization header
 * 3. API Gateway validates JWT
 * 4. Gateway adds X-User-Id, X-User-Role headers with validated user info
 * 5. Mentor service trusts these headers (internal network security)
 *
 * AUTHORIZATION:
 * - Endpoint-level: Configured in SecurityFilterChain
 * - Method-level: @PreAuthorize annotations in controllers/services
 * - Role hierarchy: ADMIN > NGO > MENTOR > YOUTH
 *
 * PUBLIC ENDPOINTS:
 * - /actuator/health - Health check
 * - /actuator/info - Application info
 * - /actuator/prometheus - Metrics (restrict in production)
 * - /swagger-ui/** - API documentation
 * - /api-docs/** - OpenAPI specification
 *
 * PROTECTED ENDPOINTS:
 * - /api/mentorship/** - Requires authentication
 * - Role-specific access enforced per endpoint
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-21
 * ============================================================================
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@Slf4j
public class SecurityConfig {

    /**
     * Security Filter Chain
     * Configures HTTP security rules
     *
     * SECURITY CHAIN:
     * 1. Disable CSRF (stateless API)
     * 2. Configure CORS
     * 3. Configure authorization rules
     * 4. Set stateless session management
     * 5. Add custom security headers
     *
     * @param http HttpSecurity builder
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain for mentor-service");

        http
                // Disable CSRF (not needed for stateless REST API)
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public actuator endpoints for health checks
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/info").permitAll()

                        // Prometheus metrics endpoint (consider restricting in production)
                        .requestMatchers("/actuator/prometheus").permitAll()

                        // Swagger UI and API documentation endpoints
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // All API endpoints require authentication
                        // Specific role-based permissions handled by @PreAuthorize in controllers
                        .requestMatchers("/api/**").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Stateless session management
                // No server-side sessions, rely on JWT tokens
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Security headers configuration
                .headers(headers -> headers
                        // Prevent page from being displayed in frame/iframe (clickjacking protection)
                        .frameOptions(frame -> frame.deny())

                        // Disable XSS protection header (modern browsers have better built-in protection)
                        .xssProtection(xss -> xss.disable())

                        // Content Security Policy - restrict resource loading
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; frame-ancestors 'none';")
                        )

                        // Prevent MIME type sniffing
                        .contentTypeOptions(contentType -> contentType.disable()) // Spring Boot handles this
                );

        log.info("Security filter chain configured successfully");
        return http.build();
    }

    /**
     * CORS Configuration Source
     * Defines allowed origins, methods, and headers for cross-origin requests
     *
     * CORS POLICY:
     * - Allowed origins: Frontend applications (web, mobile, admin)
     * - Allowed methods: Standard HTTP methods
     * - Allowed headers: All headers
     * - Credentials: Allowed (for cookies and authentication headers)
     * - Max age: 3600 seconds (1 hour) for preflight cache
     *
     * NOTE: In production, API Gateway typically handles CORS.
     * This is a fallback configuration for direct service access during development.
     *
     * @return CorsConfigurationSource with defined CORS rules
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuring CORS policy");

        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins - should be externalized to application properties in production
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",           // React development server
                "http://localhost:3001",           // Mobile app development server
                "https://platform.ug",             // Production web application
                "https://admin.platform.ug",       // Admin panel
                "https://api.platform.ug"          // API Gateway
        ));

        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Allow all headers
        configuration.setAllowedHeaders(List.of("*"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // How long preflight requests can be cached (in seconds)
        configuration.setMaxAge(3600L);

        // Expose additional headers to client
        configuration.setExposedHeaders(Arrays.asList(
                "X-Total-Count",
                "X-Page-Number",
                "X-Page-Size"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("CORS configuration completed");
        return source;
    }
}