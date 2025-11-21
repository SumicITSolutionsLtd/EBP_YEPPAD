package com.youthconnect.service_registry.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * ================================================================================
 * Security Configuration for Eureka Server with Custom Login Page
 * ================================================================================
 *
 * This configuration secures the Eureka Server dashboard with a custom-designed
 * login page instead of the default browser basic authentication popup.
 *
 * Key Features:
 * - Custom HTML login page (login.html)
 * - Form-based authentication
 * - Session management for browser access
 * - HTTP Basic auth for microservice registration (backward compatible)
 * - Public access to health endpoints for monitoring
 *
 * Authentication Flow:
 * 1. Browser Access (Dashboard):
 *    - User navigates to http://localhost:8761
 *    - Redirected to /login page with custom UI
 *    - Submits username/password via form
 *    - Spring Security validates credentials
 *    - Creates session and redirects to dashboard
 *
 * 2. Microservice Registration:
 *    - Services use HTTP Basic Auth in URL or header
 *    - Format: http://admin:changeme@localhost:8761/eureka/
 *    - No session created (stateless)
 *
 * @author EBP Development Team
 * @version 3.0.0
 * @since 2025-01-29
 * ================================================================================
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${eureka.username:admin}")
    private String username;

    @Value("${eureka.password:changeme}")
    private String password;

    /**
     * Configures HTTP Security with custom login page support.
     *
     * This configuration:
     * - Uses form login for browser access (custom login.html page)
     * - Supports HTTP Basic auth for API/microservice clients
     * - Disables CSRF for Eureka endpoints (required for service registration)
     * - Allows public access to health checks
     *
     * @param http HttpSecurity configuration object
     * @return SecurityFilterChain configured security filter chain
     * @throws Exception if security configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ═══════════════════════════════════════════════════════════════
                // CSRF PROTECTION CONFIGURATION
                // ═══════════════════════════════════════════════════════════════
                // We need to disable CSRF for Eureka API endpoints while keeping
                // it enabled for the login form (browser security)
                .csrf(csrf -> csrf
                                // Disable CSRF for Eureka API endpoints (microservice registration)
                                .ignoringRequestMatchers("/eureka/**")
                        // CSRF is still active for /login (form submission)
                )

                // ═══════════════════════════════════════════════════════════════
                // AUTHORIZATION RULES
                // ═══════════════════════════════════════════════════════════════
                .authorizeHttpRequests(authorize -> authorize
                        // ───────────────────────────────────────────────────────────
                        // PUBLIC ENDPOINTS (No Authentication Required)
                        // ───────────────────────────────────────────────────────────
                        // Allow public access to:
                        // - Login page and its assets (CSS, JS, images)
                        // - Health check endpoints (for load balancers)
                        // - Static resources (favicon, etc.)
                        .requestMatchers(
                                "/login",           // Login page
                                "/login.html",      // Direct HTML access
                                "/css/**",          // CSS files
                                "/js/**",           // JavaScript files
                                "/images/**",       // Images
                                "/favicon.ico",     // Favicon
                                "/actuator/health", // Health endpoint
                                "/actuator/health/**", // Health details
                                "/actuator/info"    // Info endpoint
                        ).permitAll()

                        // ───────────────────────────────────────────────────────────
                        // PROTECTED ENDPOINTS (Authentication Required)
                        // ───────────────────────────────────────────────────────────
                        // All other endpoints require authentication:
                        // - Eureka Dashboard (/)
                        // - Eureka API (/eureka/**)
                        // - Management endpoints (/actuator/**)
                        .anyRequest().authenticated()
                )

                // ═══════════════════════════════════════════════════════════════
                // FORM LOGIN CONFIGURATION (For Browser Access)
                // ═══════════════════════════════════════════════════════════════
                .formLogin(form -> form
                        // ───────────────────────────────────────────────────────────
                        // Custom Login Page Configuration
                        // ───────────────────────────────────────────────────────────
                        // Specify the custom login page URL
                        .loginPage("/login")

                        // The URL where the login form should POST credentials
                        // Spring Security automatically creates this endpoint
                        .loginProcessingUrl("/login")

                        // Where to redirect after successful login
                        // If user tried to access a specific page, redirect there
                        // Otherwise, redirect to Eureka dashboard
                        .defaultSuccessUrl("/", true)

                        // Where to redirect after failed login
                        // The '?error' parameter is added automatically
                        .failureUrl("/login?error=true")

                        // Form field names (must match login.html)
                        .usernameParameter("username")
                        .passwordParameter("password")

                        // Allow everyone to access the login page
                        .permitAll()
                )

                // ═══════════════════════════════════════════════════════════════
                // LOGOUT CONFIGURATION
                // ═══════════════════════════════════════════════════════════════
                .logout(logout -> logout
                        // Logout endpoint URL
                        .logoutUrl("/logout")

                        // Where to redirect after logout
                        .logoutSuccessUrl("/login?logout=true")

                        // Invalidate HTTP session
                        .invalidateHttpSession(true)

                        // Delete cookies
                        .deleteCookies("JSESSIONID")

                        // Allow everyone to access logout
                        .permitAll()
                )

                // ═══════════════════════════════════════════════════════════════
                // HTTP BASIC AUTHENTICATION (For Microservices)
                // ═══════════════════════════════════════════════════════════════
                // Keep HTTP Basic auth enabled for backward compatibility
                // Microservices will use this method (not form login)
                // Example: http://admin:changeme@localhost:8761/eureka/
                .httpBasic(Customizer.withDefaults())

                // ═══════════════════════════════════════════════════════════════
                // SESSION MANAGEMENT
                // ═══════════════════════════════════════════════════════════════
                .sessionManagement(session -> session
                        // Create session only if required (for form login)
                        // HTTP Basic requests won't create sessions
                        .sessionCreationPolicy(
                                org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED
                        )

                        // Maximum concurrent sessions per user
                        .maximumSessions(5)

                        // Prevent new logins when max sessions reached
                        .maxSessionsPreventsLogin(false)
                );

        log.info("✓ Eureka Server Security Configured");
        log.info("  - Form Login: Enabled (Custom login page at /login)");
        log.info("  - HTTP Basic: Enabled (For microservices)");
        log.info("  - CSRF: Enabled for forms, disabled for /eureka/**");

        if ("changeme".equals(password)) {
            log.warn("⚠ WARNING: Using default credentials!");
            log.warn("  Username: {}", username);
            log.warn("  Password: {} (CHANGE IN PRODUCTION!)", password);
        }

        return http.build();
    }

    /**
     * Configures in-memory user details service.
     *
     * In production, replace this with:
     * - JDBC authentication (database users)
     * - LDAP authentication (Active Directory)
     * - OAuth2/OIDC (Google, Azure AD, etc.)
     *
     * @return UserDetailsService with configured admin user
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username(username)
                .password(passwordEncoder().encode(password))
                .roles("ADMIN") // Role used for authorization if needed
                .build();

        log.info("✓ Eureka Admin User Configured: [{}]", username);

        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Password encoder using BCrypt hashing algorithm.
     *
     * BCrypt automatically handles:
     * - Salt generation
     * - Password hashing
     * - Comparison during authentication
     *
     * Strength 12 = 2^12 iterations (secure and performant)
     *
     * @return PasswordEncoder implementation
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * ════════════════════════════════════════════════════════════════════
     * TROUBLESHOOTING GUIDE
     * ════════════════════════════════════════════════════════════════════
     *
     * Issue: Login page shows "404 Not Found"
     * ────────────────────────────────────────────────────────────────────
     * Solution:
     * 1. Verify login.html exists in src/main/resources/templates/
     * 2. Check that Thymeleaf dependency is in pom.xml
     * 3. Restart the application
     *
     * Issue: "Invalid username or password" with correct credentials
     * ────────────────────────────────────────────────────────────────────
     * Solution:
     * 1. Check EUREKA_USERNAME and EUREKA_PASSWORD in .env
     * 2. Verify credentials match exactly (case-sensitive)
     * 3. Clear browser cookies/cache
     * 4. Check logs for BCrypt encoding errors
     *
     * Issue: Microservices can't register (401 Unauthorized)
     * ────────────────────────────────────────────────────────────────────
     * Solution:
     * 1. Update service URLs to include credentials:
     *    http://admin:changeme@localhost:8761/eureka/
     * 2. Verify HTTP Basic auth is enabled (see httpBasic() above)
     * 3. Check CSRF is disabled for /eureka/** endpoints
     *
     * Issue: CSRF token errors on login form
     * ────────────────────────────────────────────────────────────────────
     * Solution:
     * 1. Ensure form uses method="POST"
     * 2. Add CSRF token to form:
     *    <input type="hidden" th:name="${_csrf.parameterName}"
     *           th:value="${_csrf.token}" />
     * 3. Use Thymeleaf th:action="/login" instead of action="/login"
     *
     * Issue: Infinite redirect loop to /login
     * ────────────────────────────────────────────────────────────────────
     * Solution:
     * 1. Verify /login is in permitAll() section
     * 2. Check that login.html exists in templates folder
     * 3. Clear browser cookies
     * 4. Restart application
     *
     * ════════════════════════════════════════════════════════════════════
     */
}