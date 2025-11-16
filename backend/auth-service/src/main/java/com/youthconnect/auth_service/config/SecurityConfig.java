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
 * Security Configuration for Auth Service
 * ═══════════════════════════════════════════════════════════════════════════

 * SOLUTION: Use simple String injection with default values, then split into arrays
 *
 * @author Douglas Kings Kato
 * @since 2025-11-16
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    // ═══════════════════════════════════════════════════════════════════════
    // DEPENDENCIES (Injected via Constructor)
    // ═══════════════════════════════════════════════════════════════════════

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final OAuth2SuccessHandler oauth2SuccessHandler;
    private final PasswordEncoder passwordEncoder;

    // ═══════════════════════════════════════════════════════════════════════
    // ✅ FIXED: CORS CONFIGURATION PROPERTIES (Using String with defaults)
    // ═══════════════════════════════════════════════════════════════════════

    @Value("${app.security.allowed-origins:http://localhost:3000,http://localhost:3001,http://localhost:5173,http://localhost:4200}")
    private String allowedOriginsString;

    @Value("${app.security.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String allowedMethodsString;

    @Value("${app.security.allowed-headers:Authorization,Content-Type,X-Requested-With,Accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers}")
    private String allowedHeadersString;

    @Value("${app.security.exposed-headers:Authorization,X-Total-Count,X-Page-Number,X-Page-Size}")
    private String exposedHeadersString;

    @Value("${app.security.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.security.max-age:3600}")
    private long maxAge;

    // ═══════════════════════════════════════════════════════════════════════
    // SECURITY FILTER CHAIN
    // ═══════════════════════════════════════════════════════════════════════

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login/**",
                                "/register/**",
                                "/ussd/login/**",
                                "/validate/**",
                                "/health/**",
                                "/actuator/**",
                                "/actuator/health/**",
                                "/actuator/prometheus",
                                "/actuator/info",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/password/forgot",
                                "/password/reset/**",
                                "/password/validate-reset-token",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2SuccessHandler)
                        .failureUrl("/login?error=oauth2_failed")
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ✅ FIXED: CORS CONFIGURATION (Parse strings into arrays)
    // ═══════════════════════════════════════════════════════════════════════

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Convert comma-separated strings to lists
        configuration.setAllowedOrigins(parseCommaSeparated(allowedOriginsString));
        configuration.setAllowedMethods(parseCommaSeparated(allowedMethodsString));
        configuration.setAllowedHeaders(parseCommaSeparated(allowedHeadersString));
        configuration.setExposedHeaders(parseCommaSeparated(exposedHeadersString));

        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Helper method to parse comma-separated string into list
     *
     * @param commaSeparated Comma-separated string
     * @return List of trimmed strings
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

    // ═══════════════════════════════════════════════════════════════════════
    // AUTHENTICATION PROVIDER & MANAGER
    // ═══════════════════════════════════════════════════════════════════════

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}