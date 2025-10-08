package com.youthconnect.auth_service.config;

import com.youthconnect.auth_service.security.JwtAuthenticationEntryPoint;
import com.youthconnect.auth_service.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security Configuration for Auth Service
 *
 * Configures Spring Security for JWT-based stateless authentication:
 * - Disables CSRF (not needed for stateless JWT auth)
 * - Configures CORS for cross-origin requests
 * - Sets session management to STATELESS
 * - Defines public and protected endpoints
 * - Integrates JWT authentication filter
 * - Configures password encoding (BCrypt)
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;

    /**
     * Configure Security Filter Chain
     *
     * Defines the security rules for the application:
     * - Which endpoints are public (no authentication required)
     * - Which endpoints require authentication
     * - How authentication failures are handled
     * - Session management strategy (stateless for JWT)
     *
     * @param http HttpSecurity object to configure
     * @return SecurityFilterChain configured filter chain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF - not needed for stateless JWT authentication
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers(
                                "/login/**",
                                "/register/**",
                                "/ussd/login/**",
                                "/validate/**",
                                "/health/**",
                                "/actuator/**",
                                "/actuator/health/**",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/password/forgot",
                                "/password/reset/**",
                                "/password/validate-reset-token"
                        ).permitAll()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Configure exception handling
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // Configure session management - STATELESS for JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Add JWT authentication filter before UsernamePasswordAuthenticationFilter
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configure CORS (Cross-Origin Resource Sharing)
     *
     * Allows the frontend application to make requests to this service
     * from different origins (domains/ports).
     *
     * @return CorsConfigurationSource CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origins (frontend applications)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",      // React dev server
                "http://localhost:3001",      // Alternative React port
                "https://youthconnect.ug",    // Production domain
                "https://www.youthconnect.ug" // Production www subdomain
        ));

        // Allow specific HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Allow specific headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        // Expose specific headers to the client
        configuration.setExposedHeaders(List.of(
                "Authorization"
        ));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Password Encoder Bean
     *
     * BCrypt is a strong hashing function designed for password storage.
     * It includes a salt automatically and has a configurable work factor
     * to make brute-force attacks more difficult.
     *
     * Strength = 12 provides good security while maintaining reasonable performance.
     *
     * @return PasswordEncoder BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Authentication Provider Bean
     *
     * Configures how Spring Security authenticates users:
     * - Uses UserDetailsService to load user data
     * - Uses PasswordEncoder to verify passwords
     *
     * @return AuthenticationProvider configured authentication provider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication Manager Bean
     *
     * The AuthenticationManager is used to authenticate users during login.
     * It delegates to the configured AuthenticationProvider.
     *
     * @param config AuthenticationConfiguration from Spring Security
     * @return AuthenticationManager authentication manager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}