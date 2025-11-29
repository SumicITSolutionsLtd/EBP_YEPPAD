package com.youthconnect.auth_service.config;

import com.youthconnect.auth_service.security.JwtAuthenticationEntryPoint;
import com.youthconnect.auth_service.security.JwtAuthenticationFilter;
import com.youthconnect.auth_service.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * SECURITY CONFIGURATION (v3.1.0 - SWAGGER & ENDPOINT FIX)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Configures Spring Security filter chains, CORS, and authentication providers.
 *
 * Updates:
 * 1. Whitelisted /api/auth/** (Since global context-path was removed)
 * 2. Whitelisted Swagger V3 endpoints (/v3/api-docs/**, /swagger-ui/**)
 * 3. Whitelisted Actuator endpoints
 *
 * @author Douglas Kings Kato
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final OAuth2SuccessHandler oauth2SuccessHandler;
    private final PasswordEncoder passwordEncoder;

    // ────────────────────────────────────────────────────────────────────────
    // CORS CONFIGURATION PROPERTIES
    // ────────────────────────────────────────────────────────────────────────
    @Value("${app.security.allowed-origins:http://localhost:3000,http://localhost:3001,http://localhost:5173}")
    private String allowedOriginsString;

    @Value("${app.security.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String allowedMethodsString;

    @Value("${app.security.allowed-headers:Authorization,Content-Type,X-Requested-With}")
    private String allowedHeadersString;

    @Value("${app.security.exposed-headers:Authorization,X-Total-Count}")
    private String exposedHeadersString;

    @Value("${app.security.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.security.max-age:3600}")
    private long maxAge;

    /**
     * Defines the security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (Not needed for stateless JWT APIs)
                .csrf(AbstractHttpConfigurer::disable)

                // Enable CORS with custom configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ────────────────────────────────────────────────────────────────
                // AUTHORIZATION RULES
                // ────────────────────────────────────────────────────────────────
                .authorizeHttpRequests(auth -> auth
                        // 1. Allow Auth Endpoints
                        // Note: We removed the global context path, so specific paths are required here
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/auth/oauth2/**").permitAll()

                        // 2. Allow Swagger UI (CRITICAL)
                        // These paths allow the OpenAPI JSON docs and the Swagger UI HTML/Assets to load
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**"
                        ).permitAll()

                        // 3. Allow Actuator (Health Checks)
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/health/**").permitAll()

                        // 4. All other requests require authentication
                        .anyRequest().authenticated()
                )

                // ────────────────────────────────────────────────────────────────
                // EXCEPTION HANDLING & SESSION
                // ────────────────────────────────────────────────────────────────
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ────────────────────────────────────────────────────────────────
                // OAUTH2 LOGIN
                // ────────────────────────────────────────────────────────────────
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2SuccessHandler)
                        .failureUrl("/login?error=oauth2_failed")
                )

                // ────────────────────────────────────────────────────────────────
                // FILTERS & PROVIDERS
                // ────────────────────────────────────────────────────────────────
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS Configuration Source
     * Parses comma-separated strings from application.yml into lists.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(parseCommaSeparated(allowedOriginsString));
        configuration.setAllowedMethods(parseCommaSeparated(allowedMethodsString));
        configuration.setAllowedHeaders(parseCommaSeparated(allowedHeadersString));
        configuration.setExposedHeaders(parseCommaSeparated(exposedHeadersString));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply CORS settings to all paths
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Helper to parse comma-separated strings into List<String>
     */
    private List<String> parseCommaSeparated(String commaSeparated) {
        if (commaSeparated == null || commaSeparated.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(commaSeparated.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}