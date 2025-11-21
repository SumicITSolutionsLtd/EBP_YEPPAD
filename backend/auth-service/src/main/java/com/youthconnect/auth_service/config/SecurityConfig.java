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
 * Security Configuration
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * CORS configuration to use String injection with default values
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (Fixed)
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

    // ✅ FIXED: Use String with default values, then parse to arrays
    @Value("${app.security.allowed-origins:http://localhost:3000,http://localhost:3001}")
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
     * Security Filter Chain
     */
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
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/password/**",
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

    /**
     * ✅ FIXED: CORS Configuration (Parse strings into arrays)
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Parse comma-separated strings to lists
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
     * Parse comma-separated string into list
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

    /**
     * Authentication Provider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Authentication Manager
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}