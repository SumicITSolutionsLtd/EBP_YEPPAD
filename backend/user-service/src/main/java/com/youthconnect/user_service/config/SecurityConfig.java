package com.youthconnect.user_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * SECURITY CONFIGURATION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * This configuration manages access control for the User Service.
 *
 * FIX IMPLEMENTED (1.1): Allow Internal Connections
 * We explicitly permit requests to "/api/v1/internal/**".
 * This allows the Auth Service (which calls this endpoint to create users)
 * to communicate with the User Service without being blocked by a 403 Forbidden error.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF and CORS.
                // This service sits behind an API Gateway which handles CORS.
                // Since we use stateless REST APIs, CSRF protection is not required here.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // 1. Allow Swagger UI, API Docs, and Actuator (Public/Dev usage)
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/**"
                        ).permitAll()

                        // 2. ✅ CRITICAL FIX: ALLOW INTERNAL ENDPOINTS
                        // These endpoints are called directly by the Auth Service (server-to-server).
                        // Since the Auth Service creates the user here *before* a token exists,
                        // this endpoint must be publicly accessible (internally).
                        .requestMatchers("/api/v1/internal/**").permitAll()

                        // Legacy/Alternative internal path support
                        .requestMatchers("/api/v1/users/internal/**").permitAll()

                        // 3. SECURE ALL OTHER REQUESTS
                        // Any other request (User Profile, Updates, etc.) must be authenticated.
                        // The GatewayAuthenticationFilter below checks for the X-User-Id header
                        // injected by the API Gateway.
                        .anyRequest().authenticated()
                )

                // Use Stateless sessions (No JSESSIONID cookies created)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Add the custom filter to trust Gateway headers (X-User-Id, X-Role)
                // This executes BEFORE the standard Spring Security authentication filter
                .addFilterBefore(new GatewayAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Custom Filter: GatewayAuthenticationFilter
     *
     * This filter assumes the API Gateway has already validated the JWT/Auth token
     * and has passed the authenticated user details downstream via HTTP Headers.
     *
     * In Production: The network configuration must ensure that only the Gateway
     * can talk to this service port, preventing external users from spoofing these headers.
     */
    public static class GatewayAuthenticationFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            // 1. Extract headers set by the API Gateway
            String userId = request.getHeader("X-User-Id");
            String role = request.getHeader("X-Role"); // e.g., "YOUTH", "ADMIN"

            // 2. If headers exist and no authentication is currently set in context
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Ensure a role is set (default to ROLE_USER if missing)
                String effectiveRole = (role != null && !role.isEmpty()) ? role : "ROLE_USER";

                // Create Authority (Spring Security usually expects "ROLE_" prefix,
                // but we trust the Gateway's string here for simplicity)
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(effectiveRole);

                // Create an Authentication object using the User ID as the principal
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userId, // Principal (The UUID)
                        null,   // Credentials (null because Gateway handled it)
                        Collections.singletonList(authority) // Authorities
                );

                // 3. Set the Security Context so .authenticated() permits the request
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            // Continue the filter chain
            filterChain.doFilter(request, response);
        }
    }
}