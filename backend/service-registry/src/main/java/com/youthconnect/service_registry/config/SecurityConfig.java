package com.youthconnect.service_registry.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security Configuration for Eureka Server (Spring Boot 3.x compatible).
 *
 * <p>Implements Basic Authentication to secure the Eureka dashboard and REST API.
 * In production, only authenticated clients can:
 * <ul>
 *   <li>Register services</li>
 *   <li>Access Eureka dashboard</li>
 *   <li>Query service registry</li>
 *   <li>View health information</li>
 * </ul>
 *
 * @author YouthConnect Uganda Development Team
 * @version 3.0.0
 */
@Slf4j
@Configuration
@EnableWebSecurity
@Profile("prod") // Only enable in production
public class SecurityConfig {

    @Value("${eureka.security.username:admin}")
    private String username;

    @Value("${eureka.security.password:changeme}")
    private String password;

    /**
     * Configures HTTP security for Eureka Server (Spring Boot 3.x).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for service-to-service communication (Spring Boot 3.x method)
                .csrf(AbstractHttpConfigurer::disable)

                // Configure authorization rules
                .authorizeHttpRequests(authorize -> authorize
                        // Public health endpoints (for load balancers/monitoring)
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Enable HTTP Basic Authentication (Spring Boot 3.x method)
                .httpBasic(Customizer.withDefaults());

        log.info("✓ Eureka Server Security Enabled (Basic Auth)");
        log.warn("⚠ Ensure eureka.security.username and eureka.security.password are set!");

        return http.build();
    }

    /**
     * Configures in-memory user for authentication.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username(username)
                .password(passwordEncoder().encode(password))
                .roles("ADMIN")
                .build();

        log.info("✓ Eureka Admin User Configured: {}", username);

        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Password encoder using BCrypt with strength 12.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}