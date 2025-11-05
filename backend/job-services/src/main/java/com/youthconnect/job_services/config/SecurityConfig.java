package com.youthconnect.job_services.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

/**
 * Security Configuration for Job Services - API Gateway Authentication Model
 *
 * ════════════════════════════════════════════════════════════════════════════
 * ARCHITECTURE OVERVIEW
 * ════════════════════════════════════════════════════════════════════════════
 *
 * This service implements a TRUST-BASED security model where authentication is
 * delegated to the API Gateway. This is a common microservices pattern that:
 *
 * 1. Centralizes authentication logic (single source of truth)
 * 2. Reduces latency (no redundant JWT validation)
 * 3. Simplifies service code (no JWT libraries needed)
 * 4. Improves scalability (services are stateless)
 *
 * ════════════════════════════════════════════════════════════════════════════
 * AUTHENTICATION FLOW
 * ════════════════════════════════════════════════════════════════════════════
 *
 * ┌─────────┐         ┌─────────────┐         ┌──────────────┐
 * │ Client  │────────▶│ API Gateway │────────▶│ Job Service  │
 * └─────────┘         └─────────────┘         └──────────────┘
 *      │                     │                        │
 *      │  1. JWT Token       │                        │
 *      │─────────────────────▶                        │
 *      │                     │                        │
 *      │                2. Validate JWT               │
 *      │                     │                        │
 *      │                3. Extract User Info          │
 *      │                     │                        │
 *      │                     │  4. Forward Request    │
 *      │                     │    + Headers:          │
 *      │                     │    X-User-Id           │
 *      │                     │    X-User-Role         │
 *      │                     │────────────────────────▶
 *      │                     │                        │
 *      │                     │         5. Trust Headers
 *      │                     │                        │
 *      │                     │         6. Process Request
 *      │                     │                        │
 *      │                     │  7. Response           │
 *      │◀────────────────────│────────────────────────│
 *
 * ════════════════════════════════════════════════════════════════════════════
 * SECURITY MODEL
 * ════════════════════════════════════════════════════════════════════════════
 *
 * ⚠️ CRITICAL ASSUMPTIONS:
 *
 * 1. NETWORK ISOLATION:
 *    - This service MUST be deployed in a private network
 *    - ONLY the API Gateway should have direct access
 *    - Use VPC, security groups, or firewall rules
 *    - Public exposure = CRITICAL SECURITY VULNERABILITY
 *
 * 2. HEADER TRUST:
 *    - We trust X-User-Id and X-User-Role headers COMPLETELY
 *    - Gateway MUST be the ONLY entity that can add these headers
 *    - Network-level security MUST prevent header spoofing
 *
 * 3. PRODUCTION ENHANCEMENTS (Recommended):
 *    - Implement header signature validation
 *    - Use shared secret between gateway and services
 *    - Add request timestamps and nonces
 *    - Example: X-Gateway-Signature = HMAC-SHA256(headers + timestamp + secret)
 *
 * ════════════════════════════════════════════════════════════════════════════
 * ENDPOINT SECURITY MATRIX
 * ════════════════════════════════════════════════════════════════════════════
 *
 * PUBLIC (No Authentication):
 * ┌────────────────────────────┬─────────────────────────────────────────┐
 * │ Endpoint Pattern           │ Purpose                                 │
 * ├────────────────────────────┼─────────────────────────────────────────┤
 * │ /health, /health/**        │ Load balancer health checks             │
 * │ /actuator/health/**        │ Spring Actuator monitoring              │
 * │ /swagger-ui/**             │ API documentation (dev only)            │
 * │ /v3/api-docs/**            │ OpenAPI specification                   │
 * │ OPTIONS /**                │ CORS preflight requests                 │
 * └────────────────────────────┴─────────────────────────────────────────┘
 *
 * PROTECTED (Requires Gateway Headers):
 * ┌────────────────────────────┬─────────────────────────────────────────┐
 * │ Endpoint Pattern           │ Required Headers                        │
 * ├────────────────────────────┼─────────────────────────────────────────┤
 * │ /api/v1/jobs/**            │ X-User-Id (optional for GET)            │
 * │ /api/v1/applications/**    │ X-User-Id (required)                    │
 * │ /api/v1/categories/**      │ X-User-Id (optional for GET)            │
 * └────────────────────────────┴─────────────────────────────────────────┘
 *
 * ════════════════════════════════════════════════════════════════════════════
 * VERSION HISTORY
 * ════════════════════════════════════════════════════════════════════════════
 *
 * Version 1.0-2.0 (Deprecated):
 * - Each service validated JWT tokens independently
 * - JWT filter chain in every microservice
 * - Redundant validation across services
 * - Higher latency and code duplication
 *
 * Version 3.0+ (Current):
 * - API Gateway handles ALL authentication
 * - Services trust gateway headers
 * - Simplified service code
 * - Better performance and maintainability
 *
 * @author Douglas Kings Kato
 * @version 3.0.0 (API Gateway Authentication)
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Enables @PreAuthorize, @Secured, @RolesAllowed
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Main Security Filter Chain Configuration
     *
     * This method configures the security filter chain for the entire application.
     * It defines which endpoints are public vs. protected, and how authentication
     * and authorization should be handled.
     *
     * KEY CONFIGURATION AREAS:
     * 1. CSRF Protection (disabled for stateless API)
     * 2. CORS Configuration (for frontend integration)
     * 3. Session Management (stateless for JWT)
     * 4. Authorization Rules (public vs. protected endpoints)
     *
     * ═══════════════════════════════════════════════════════════════════════
     * IMPORTANT CHANGES FROM PREVIOUS VERSION:
     * ═══════════════════════════════════════════════════════════════════════
     *
     * ❌ REMOVED: JwtAuthenticationFilter
     *    - No longer validating JWT tokens in this service
     *    - No JWT parsing or signature verification
     *    - No token expiration checking
     *
     * ✅ ADDED: Trust-based authentication
     *    - Trust X-User-Id and X-User-Role headers from gateway
     *    - Extract user context using GatewayUserContextUtil
     *    - Enforce authorization at service method level
     *
     * @param http HttpSecurity builder for configuring security
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ═══════════════════════════════════════════════════════════════
                // CSRF PROTECTION CONFIGURATION
                // ═══════════════════════════════════════════════════════════════
                //
                // CSRF (Cross-Site Request Forgery) protection is DISABLED because:
                //
                // 1. Stateless API: We don't use cookies for authentication
                // 2. Token-based auth: JWT in Authorization header (not vulnerable to CSRF)
                // 3. Gateway validation: API Gateway validates tokens before forwarding
                // 4. CORS protection: We use CORS headers for cross-origin protection
                //
                // ⚠️ NOTE: If you ever add cookie-based authentication, RE-ENABLE CSRF!
                //
                .csrf(AbstractHttpConfigurer::disable)

                // ═══════════════════════════════════════════════════════════════
                // CORS (Cross-Origin Resource Sharing) CONFIGURATION
                // ═══════════════════════════════════════════════════════════════
                //
                // CORS allows frontend applications running on different domains
                // to make requests to this API. Configuration is defined in the
                // corsConfigurationSource() method below.
                //
                // Security considerations:
                // - Specific origins only (no wildcards in production)
                // - Credentials allowed (for Authorization headers)
                // - Preflight caching for performance
                //
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ═══════════════════════════════════════════════════════════════
                // SESSION MANAGEMENT CONFIGURATION
                // ═══════════════════════════════════════════════════════════════
                //
                // STATELESS session policy means:
                //
                // 1. No server-side session storage
                // 2. No JSESSIONID cookies
                // 3. Each request must contain authentication headers
                // 4. Enables horizontal scaling without session affinity
                // 5. Better for microservices architecture
                //
                // Gateway headers provide user context on EVERY request.
                // No need to maintain session state.
                //
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ═══════════════════════════════════════════════════════════════
                // AUTHORIZATION RULES
                // ═══════════════════════════════════════════════════════════════
                //
                // This section defines which endpoints require authentication
                // and which are publicly accessible.
                //
                .authorizeHttpRequests(auth -> auth

                        // ───────────────────────────────────────────────────────────
                        // PUBLIC ENDPOINTS (No Authentication Required)
                        // ───────────────────────────────────────────────────────────
                        //
                        // These endpoints MUST be public for infrastructure to work:

                        // 1. HEALTH CHECKS
                        //    - Required by: Load balancers, Kubernetes, Docker
                        //    - Purpose: Monitor service availability
                        //    - Security: No sensitive data exposed
                        .requestMatchers("/health", "/health/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()

                        // 2. API DOCUMENTATION
                        //    - Swagger UI and OpenAPI specs
                        //    - ⚠️ Consider restricting in production!
                        //    - Options: IP whitelist, VPN, or internal-only access
                        .requestMatchers(
                                "/swagger-ui/**",      // Swagger UI static files
                                "/v3/api-docs/**",     // OpenAPI 3.0 specification
                                "/swagger-ui.html",    // Swagger UI HTML page
                                "/api-docs/**"         // Additional API docs
                        ).permitAll()

                        // 3. CORS PREFLIGHT REQUESTS
                        //    - OPTIONS requests must be public for CORS to work
                        //    - Browser sends OPTIONS before actual request
                        //    - Response includes CORS headers for validation
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ───────────────────────────────────────────────────────────
                        // PROTECTED ENDPOINTS (Authentication Required)
                        // ───────────────────────────────────────────────────────────
                        //
                        // ALL other endpoints require authentication via API Gateway.
                        //
                        // Authentication flow:
                        // 1. Gateway validates JWT token
                        // 2. Gateway adds X-User-Id and X-User-Role headers
                        // 3. Request forwarded to this service
                        // 4. GatewayUserContextUtil extracts user context
                        // 5. Service methods check authorization
                        //
                        // Authorization is enforced at TWO levels:
                        //
                        // A. Service Method Level (Recommended):
                        //    - Use @PreAuthorize annotations
                        //    - Check user ID ownership
                        //    - Validate user roles
                        //    - Example:
                        //      @PreAuthorize("hasRole('COMPANY')")
                        //      public Job createJob(...) { }
                        //
                        // B. Manual Authorization (Common):
                        //    - Extract user context in controller
                        //    - Pass to service layer
                        //    - Service validates ownership/permissions
                        //    - Example:
                        //      UUID userId = GatewayUserContextUtil.getCurrentUserId();
                        //      jobService.deleteJob(jobId, userId); // Service checks ownership
                        //
                        .anyRequest().authenticated()
                );

        // ═══════════════════════════════════════════════════════════════════
        // REMOVED: JWT AUTHENTICATION FILTER
        // ═══════════════════════════════════════════════════════════════════
        //
        // ❌ OLD CODE (Version 1.0-2.0):
        //
        //    @Autowired
        //    private JwtAuthenticationFilter jwtAuthenticationFilter;
        //
        //    http.addFilterBefore(
        //        jwtAuthenticationFilter,
        //        UsernamePasswordAuthenticationFilter.class
        //    );
        //
        // ✅ NEW APPROACH (Version 3.0+):
        //
        //    - No JWT filter in this service
        //    - API Gateway handles ALL token validation
        //    - This service trusts gateway headers
        //    - Simpler, faster, more maintainable
        //
        // ═══════════════════════════════════════════════════════════════════

        return http.build();
    }

    /**
     * CORS Configuration for Frontend Integration
     *
     * Cross-Origin Resource Sharing (CORS) allows frontend applications running
     * on different domains to make requests to this API.
     *
     * ═══════════════════════════════════════════════════════════════════════
     * IMPORTANT: PRODUCTION CONSIDERATIONS
     * ═══════════════════════════════════════════════════════════════════════
     *
     * ⚠️ CRITICAL: In production, these origins should match your API GATEWAY
     * URL, NOT this service's URL!
     *
     * Request flow:
     * Frontend (https://youthconnect.ug)
     *    ↓
     * API Gateway (https://api.youthconnect.ug)
     *    ↓
     * Job Service (http://job-service:8000) ← Internal only!
     *
     * The frontend NEVER communicates directly with this service.
     * All requests go through the API Gateway.
     *
     * ═══════════════════════════════════════════════════════════════════════
     * CORS SECURITY MODEL
     * ═══════════════════════════════════════════════════════════════════════
     *
     * 1. ALLOWED ORIGINS:
     *    - Specific domains only (never use "*" in production)
     *    - Must include protocol (http:// or https://)
     *    - Port numbers matter (localhost:3000 ≠ localhost:3001)
     *
     * 2. CREDENTIALS:
     *    - setAllowCredentials(true) allows:
     *      * Authorization headers
     *      * Cookies (if used)
     *      * Client certificates
     *    - Required for JWT authentication
     *
     * 3. PREFLIGHT CACHING:
     *    - Browsers cache preflight responses for 1 hour
     *    - Reduces OPTIONS requests
     *    - Improves performance
     *
     * @return Configured CorsConfigurationSource for all endpoints
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ═══════════════════════════════════════════════════════════════════
        // ALLOWED ORIGINS
        // ═══════════════════════════════════════════════════════════════════
        //
        // List of domains that can make cross-origin requests to this API.
        //
        // ⚠️ SECURITY WARNING:
        // - NEVER use "*" (wildcard) in production!
        // - Specify exact origins for security
        // - Include ALL frontend deployment URLs
        //
        // Development Origins:
        // - http://localhost:3000  → React development server
        // - http://localhost:4200  → Angular development server
        // - http://localhost:8088  → API Gateway (local testing)
        //
        // Production Origins:
        // - https://youthconnect.ug     → Production frontend
        // - https://api.youthconnect.ug → Production API Gateway
        //
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",      // React dev
                "http://localhost:4200",      // Angular dev
                "http://localhost:8088",      // Gateway dev
                "https://youthconnect.ug",    // Production frontend
                "https://api.youthconnect.ug" // Production API
        ));

        // ═══════════════════════════════════════════════════════════════════
        // ALLOWED HTTP METHODS
        // ═══════════════════════════════════════════════════════════════════
        //
        // Standard REST methods supported by this API.
        //
        // GET     → Read operations (fetch jobs, get details)
        // POST    → Create operations (submit application)
        // PUT     → Full update operations (update job)
        // PATCH   → Partial update operations (update status)
        // DELETE  → Delete operations (withdraw application)
        // OPTIONS → CORS preflight (browser automatic)
        //
        configuration.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        // ═══════════════════════════════════════════════════════════════════
        // ALLOWED HEADERS
        // ═══════════════════════════════════════════════════════════════════
        //
        // Headers that frontend applications can include in requests.
        //
        // Standard Headers:
        // - Authorization: JWT token (for gateway)
        // - Content-Type: Request body format (application/json)
        // - Accept: Desired response format
        //
        // Gateway Headers (Internal):
        // - X-User-Id: User UUID (added by gateway)
        // - X-User-Role: User role (added by gateway)
        // - X-Auth-Token: Original JWT (optional, for audit)
        //
        // ⚠️ NOTE: Frontend should NOT send X-User-* headers!
        // Only the API Gateway should add these headers.
        //
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",                  // JWT token
                "Content-Type",                   // Request format
                "X-Requested-With",              // AJAX indicator
                "Accept",                        // Response format
                "Origin",                        // Request origin
                "Access-Control-Request-Method", // Preflight
                "Access-Control-Request-Headers",// Preflight
                "X-User-Id",                     // Gateway header
                "X-User-Role",                   // Gateway header
                "X-Auth-Token"                   // Gateway header
        ));

        // ═══════════════════════════════════════════════════════════════════
        // EXPOSED HEADERS
        // ═══════════════════════════════════════════════════════════════════
        //
        // Headers that browser JavaScript can access in responses.
        //
        // By default, browsers only expose simple response headers:
        // - Cache-Control
        // - Content-Language
        // - Content-Type
        // - Expires
        // - Last-Modified
        // - Pragma
        //
        // We need to explicitly expose:
        // - Authorization: For token refresh flows
        // - Content-Disposition: For file download names
        // - X-Total-Count: For pagination metadata
        //
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Disposition",
                "X-Total-Count"
        ));

        // ═══════════════════════════════════════════════════════════════════
        // CREDENTIALS AND CACHING
        // ═══════════════════════════════════════════════════════════════════

        // Allow credentials (Authorization header, cookies)
        // Required for JWT authentication to work
        configuration.setAllowCredentials(true);

        // Cache preflight responses for 1 hour (3600 seconds)
        // Reduces number of OPTIONS requests
        // Browser automatically sends OPTIONS before actual request
        // Caching improves performance significantly
        configuration.setMaxAge(3600L);

        // ═══════════════════════════════════════════════════════════════════
        // REGISTER CONFIGURATION FOR ALL ENDPOINTS
        // ═══════════════════════════════════════════════════════════════════

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}