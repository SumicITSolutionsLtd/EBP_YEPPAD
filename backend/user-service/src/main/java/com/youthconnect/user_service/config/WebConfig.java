package com.youthconnect.user_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.PathResourceResolver;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Web Configuration for Youth Connect Uganda User Service
 *
 * <p>This comprehensive web configuration handles all aspects of HTTP request/response
 * processing for the Youth Connect Uganda platform including:</p>
 *
 * <ul>
 *   <li>CORS configuration for frontend integration</li>
 *   <li>JSON serialization with proper date/time handling</li>
 *   <li>Request/response logging and monitoring</li>
 *   <li>Static resource serving</li>
 *   <li>File upload handling</li>
 *   <li>Content negotiation</li>
 *   <li>Custom interceptors for metrics and security</li>
 *   <li>Error handling and response formatting</li>
 * </ul>
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Multi-environment CORS settings</li>
 *   <li>Request performance monitoring</li>
 *   <li>Security headers injection</li>
 *   <li>File upload optimization</li>
 *   <li>API versioning support</li>
 *   <li>Comprehensive logging</li>
 * </ul>
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
 */
@Slf4j
@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ApplicationProperties applicationProperties;

    /**
     * CORS Configuration for secure cross-origin requests
     *
     * <p>Configures Cross-Origin Resource Sharing (CORS) to allow the frontend
     * applications to safely communicate with this API from different domains.
     * Supports multiple frontend environments and development setups.</p>
     *
     * <p><strong>Security Considerations:</strong></p>
     * <ul>
     *   <li>Only whitelisted origins are allowed</li>
     *   <li>Credentials (cookies) can be included when configured</li>
     *   <li>Specific HTTP methods are allowed per endpoint pattern</li>
     *   <li>Custom headers are exposed for frontend consumption</li>
     * </ul>
     *
     * @param registry CorsRegistry to configure CORS mappings
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var corsConfig = applicationProperties.getSecurity().getCors();

        log.info("Configuring CORS for {} origins", corsConfig.getAllowedOrigins().size());

        // Main API CORS configuration - most permissive for authenticated requests
        registry.addMapping("/api/**")
                .allowedOriginPatterns(corsConfig.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods(corsConfig.getAllowedMethods().toArray(new String[0]))
                .allowedHeaders(corsConfig.getAllowedHeaders().toArray(new String[0]))
                .exposedHeaders(corsConfig.getExposedHeaders().toArray(new String[0]))
                .allowCredentials(corsConfig.isAllowCredentials())
                .maxAge(corsConfig.getMaxAge());

        // Actuator endpoints CORS - more restrictive, limited to monitoring tools
        registry.addMapping("/actuator/**")
                .allowedOriginPatterns("http://localhost:*", "https://*.youthconnect.ug")
                .allowedMethods("GET", "POST")
                .allowCredentials(false)
                .maxAge(corsConfig.getMaxAge());

        // Documentation endpoints CORS - public access for API documentation
        registry.addMapping("/swagger-ui/**", "/v3/api-docs/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET")
                .allowCredentials(false)
                .maxAge(corsConfig.getMaxAge());

        log.debug("CORS configuration applied for all endpoint patterns");
    }

    /**
     * Static Resource Handler Configuration
     *
     * <p>Manages file serving, caching, and resource optimization for various
     * types of static content including documentation, user uploads, and web assets.</p>
     *
     * <p><strong>Caching Strategy:</strong></p>
     * <ul>
     *   <li>Swagger UI: No cache in dev, 1 hour in production</li>
     *   <li>Static assets: 1 year (immutable content with versioned URLs)</li>
     *   <li>User uploads: 1 day (may change but not frequently)</li>
     *   <li>Documentation: 1 hour (updated occasionally)</li>
     * </ul>
     *
     * @param registry ResourceHandlerRegistry to configure resource handlers
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        var uploadConfig = applicationProperties.getUpload();

        log.info("Configuring resource handlers for upload directory: {}", uploadConfig.getUploadDir());

        // Swagger UI resources with no caching for development
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")
                .setCachePeriod(applicationProperties.getEnvironment().equals("development") ? 0 : 3600)
                .resourceChain(true);

        // Static web assets with long-term caching (versioned files)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(31556926) // 1 year in seconds
                .resourceChain(true)
                .addResolver(new PathResourceResolver());

        // User uploaded files with medium-term caching - FIXED: proper path formatting
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadConfig.getUploadDir() + "/")
                .setCachePeriod(86400) // 1 day in seconds
                .resourceChain(false); // Don't chain for user uploads to avoid conflicts

        // API documentation resources
        registry.addResourceHandler("/docs/**")
                .addResourceLocations("classpath:/static/docs/")
                .setCachePeriod(3600); // 1 hour in seconds

        log.debug("Resource handlers configured for static content, uploads, and documentation");
    }

    /**
     * Custom ObjectMapper for consistent JSON serialization
     *
     * <p>Configures Jackson ObjectMapper with Uganda-specific settings for
     * proper date/time handling, null value serialization, and API evolution support.</p>
     *
     * <p><strong>Key Configurations:</strong></p>
     * <ul>
     *   <li>JavaTimeModule for LocalDateTime/ZonedDateTime support</li>
     *   <li>ISO-8601 date format with timezone information</li>
     *   <li>Graceful handling of unknown properties (API evolution)</li>
     *   <li>Pretty printing in development mode</li>
     *   <li>Custom null value serialization</li>
     * </ul>
     *
     * @return Configured ObjectMapper instance
     */
    @Bean
    public ObjectMapper customObjectMapper() {
        log.info("Configuring custom ObjectMapper for JSON processing");

        ObjectMapper objectMapper = new ObjectMapper();

        // Register JavaTimeModule for proper LocalDateTime, ZonedDateTime handling
        objectMapper.registerModule(new JavaTimeModule());

        // Configure date/time serialization - ISO-8601 format with timezone
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);

        // Handle missing properties gracefully for API evolution
        // Allows clients with newer API versions to send fields not yet in this version
        objectMapper.configure(
                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
        );

        // Handle empty beans gracefully for better API responses
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // Pretty print JSON in development for easier debugging
        if ("development".equals(applicationProperties.getEnvironment())) {
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        }

        // Configure custom null value handling
        objectMapper.getSerializerProvider().setNullValueSerializer(
                new com.fasterxml.jackson.databind.JsonSerializer<Object>() {
                    @Override
                    public void serialize(Object value,
                                          com.fasterxml.jackson.core.JsonGenerator gen,
                                          com.fasterxml.jackson.databind.SerializerProvider serializers)
                            throws IOException {
                        gen.writeNull();
                    }
                }
        );

        log.debug("ObjectMapper configured with custom serialization rules");
        return objectMapper;
    }

    /**
     * HTTP Message Converters Configuration
     *
     * <p>Customizes how request and response bodies are converted between
     * HTTP messages and Java objects. Primarily configures JSON conversion
     * with our custom ObjectMapper.</p>
     *
     * @param converters List of HTTP message converters to configure
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.debug("Configuring HTTP message converters");

        // JSON converter with custom ObjectMapper
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(customObjectMapper());
        jsonConverter.setPrettyPrint("development".equals(applicationProperties.getEnvironment()));

        // Add at beginning for priority over default converters
        converters.add(0, jsonConverter);

        log.debug("JSON message converter configured with custom ObjectMapper");
    }

    /**
     * Content Negotiation Configuration
     *
     * <p>Configures how the API determines response format based on client requests.
     * Supports multiple formats: JSON (default), XML, and CSV.</p>
     *
     * <p><strong>Strategy:</strong> Uses Accept header only, no URL extensions or parameters</p>
     *
     * @param configurer ContentNegotiationConfigurer to set up negotiation rules
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        log.debug("Configuring content negotiation");

        configurer
                .favorParameter(false) // Don't use ?format=json parameters
                .favorPathExtension(false) // Don't use .json extensions (deprecated in Spring)
                .ignoreAcceptHeader(false) // Use Accept header for format negotiation
                .defaultContentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .mediaType("json", org.springframework.http.MediaType.APPLICATION_JSON)
                .mediaType("xml", org.springframework.http.MediaType.APPLICATION_XML)
                .mediaType("csv", org.springframework.http.MediaType.parseMediaType("text/csv"));
    }

    /**
     * Request Interceptor Configuration
     *
     * <p>Registers custom interceptors that execute before and after request handling.
     * These interceptors add cross-cutting concerns like logging, metrics, security headers.</p>
     *
     * <p><strong>Registered Interceptors:</strong></p>
     * <ol>
     *   <li>PerformanceMonitoringInterceptor - Tracks request duration</li>
     *   <li>SecurityHeadersInterceptor - Adds security headers</li>
     *   <li>RateLimitingInterceptor - Rate limit protection (if enabled)</li>
     *   <li>ApiVersioningInterceptor - API version handling</li>
     * </ol>
     *
     * @param registry InterceptorRegistry to add interceptors
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("Configuring request interceptors");

        // Performance monitoring interceptor - tracks request duration
        registry.addInterceptor(new PerformanceMonitoringInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/health", "/actuator/**");

        // Security headers interceptor - adds security headers to all responses
        registry.addInterceptor(new SecurityHeadersInterceptor())
                .addPathPatterns("/**");

        // Rate limiting interceptor - protects against abuse (if enabled)
        if (applicationProperties.getSecurity().getRateLimit().isEnabled()) {
            registry.addInterceptor(new RateLimitingInterceptor())
                    .addPathPatterns("/api/**")
                    .excludePathPatterns("/api/auth/health");
        }

        // API versioning interceptor - handles version routing
        registry.addInterceptor(new ApiVersioningInterceptor())
                .addPathPatterns("/api/**");

        log.debug("Configured {} request interceptors", 3);
    }

    /**
     * View Controller Configuration
     *
     * <p>Maps simple URL patterns to views or redirects without requiring
     * dedicated controller methods. Used for convenience endpoints.</p>
     *
     * @param registry ViewControllerRegistry to add view controllers
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        log.debug("Configuring view controllers");

        // Redirect root to API documentation
        registry.addRedirectViewController("/", "/swagger-ui.html");
        registry.addRedirectViewController("/docs", "/swagger-ui.html");
        registry.addRedirectViewController("/api", "/swagger-ui.html");

        // Health check shortcuts
        registry.addRedirectViewController("/health", "/actuator/health");
        registry.addRedirectViewController("/status", "/actuator/health");
    }

    /**
     * Request Logging Filter
     *
     * <p>Configures detailed HTTP request/response logging for debugging and audit purposes.
     * Logs query strings, headers, client info, and optionally request payloads.</p>
     *
     * <p><strong>FIXED:</strong> FilterRegistrationBean now uses single-argument constructor</p>
     *
     * @return Configured FilterRegistrationBean for request logging
     */
    @Bean
    public FilterRegistrationBean<CommonsRequestLoggingFilter> requestLoggingFilter() {
        log.info("Configuring request logging filter");

        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(applicationProperties.getAudit().isIncludeRequestBody());
        filter.setIncludeHeaders(true);
        filter.setIncludeClientInfo(true);
        filter.setMaxPayloadLength(1000); // Limit payload logging size for performance

        // FIXED: Use single-argument constructor
        FilterRegistrationBean<CommonsRequestLoggingFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);

        return registration;
    }

    /**
     * Security Headers Filter
     *
     * <p>Registers a filter that adds essential security headers to all HTTP responses.
     * Protects against common web vulnerabilities like XSS, clickjacking, and MIME sniffing.</p>
     *
     * <p><strong>FIXED:</strong> FilterRegistrationBean now uses single-argument constructor</p>
     *
     * @return Configured FilterRegistrationBean for security headers
     */
    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        log.info("Configuring security headers filter");

        // FIXED: Use single-argument constructor
        FilterRegistrationBean<SecurityHeadersFilter> registration =
                new FilterRegistrationBean<>(new SecurityHeadersFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(2);

        return registration;
    }

    // =========================================================================
    // INTERCEPTOR IMPLEMENTATIONS
    // =========================================================================

    /**
     * Performance Monitoring Interceptor
     *
     * <p>Tracks request duration and logs performance metrics. Identifies slow
     * requests for optimization and maintains request/response statistics.</p>
     *
     * <p><strong>Metrics Tracked:</strong></p>
     * <ul>
     *   <li>Request start time</li>
     *   <li>Request duration in milliseconds</li>
     *   <li>HTTP status code</li>
     *   <li>Client IP address</li>
     *   <li>Slow request warnings (>5 seconds)</li>
     * </ul>
     */
    public static class PerformanceMonitoringInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object handler) {
            // Record start time for duration calculation
            long startTime = System.currentTimeMillis();
            request.setAttribute("startTime", startTime);

            String method = request.getMethod();
            String uri = request.getRequestURI();
            String clientIp = getClientIpAddress(request);

            log.info("Request started: {} {} from {}", method, uri, clientIp);
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Object handler,
                                    Exception ex) {
            // Calculate request duration
            Long startTime = (Long) request.getAttribute("startTime");
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                String method = request.getMethod();
                String uri = request.getRequestURI();
                int status = response.getStatus();

                log.info("Request completed: {} {} - Status: {} - Duration: {}ms",
                        method, uri, status, duration);

                // Log slow requests for performance monitoring
                if (duration > 5000) {
                    log.warn("Slow request detected: {} {} took {}ms", method, uri, duration);
                }

                // TODO: Record metrics for monitoring dashboards
                // metricsHelper.recordResponseTime(duration, uri, status);
            }

            if (ex != null) {
                log.error("Request failed with exception: {}", ex.getMessage());
            }
        }

        /**
         * Extracts client IP address considering proxy headers
         *
         * @param request HTTP request
         * @return Client IP address
         */
        private String getClientIpAddress(HttpServletRequest request) {
            // Check X-Forwarded-For header (set by proxies/load balancers)
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
                return xForwardedFor.split(",")[0].trim();
            }

            // Check X-Real-IP header (alternative proxy header)
            String xRealIP = request.getHeader("X-Real-IP");
            if (xRealIP != null && !xRealIP.isEmpty() && !"unknown".equalsIgnoreCase(xRealIP)) {
                return xRealIP;
            }

            // Fallback to remote address
            return request.getRemoteAddr();
        }
    }

    /**
     * Security Headers Interceptor
     *
     * <p>Adds security headers to protect against common web vulnerabilities
     * including XSS, clickjacking, MIME sniffing, and insecure referrers.</p>
     *
     * <p><strong>Headers Added:</strong></p>
     * <ul>
     *   <li>Content-Security-Policy - XSS protection</li>
     *   <li>X-Frame-Options - Clickjacking protection</li>
     *   <li>X-XSS-Protection - Browser XSS filter</li>
     *   <li>X-Content-Type-Options - MIME sniffing protection</li>
     *   <li>Referrer-Policy - Referrer information control</li>
     *   <li>Strict-Transport-Security - HTTPS enforcement (HTTPS only)</li>
     * </ul>
     */
    public static class SecurityHeadersInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object handler) {
            // Content Security Policy - prevents XSS attacks
            response.setHeader("Content-Security-Policy",
                    "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'");

            // Prevent clickjacking attacks
            response.setHeader("X-Frame-Options", "DENY");

            // Enable browser XSS protection
            response.setHeader("X-XSS-Protection", "1; mode=block");

            // Prevent MIME type sniffing
            response.setHeader("X-Content-Type-Options", "nosniff");

            // Control referrer information
            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

            // HSTS for HTTPS deployments - force HTTPS for 1 year
            if (request.isSecure()) {
                response.setHeader("Strict-Transport-Security",
                        "max-age=31536000; includeSubDomains");
            }

            return true;
        }
    }

    /**
     * Rate Limiting Interceptor
     *
     * <p>Implements request rate limiting to protect against abuse and DDoS attacks.
     * Uses token bucket algorithm based on client IP address.</p>
     *
     * <p><strong>Note:</strong> This is a placeholder implementation.
     * Production should use Redis-based rate limiting for distributed systems.</p>
     */
    public static class RateLimitingInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object handler) throws IOException {
            String clientIp = getClientIpAddress(request);

            // TODO: Implement actual rate limiting logic with Redis
            // Current implementation just logs for monitoring
            log.debug("Rate limit check for IP: {}", clientIp);

            // Example of rate limit exceeded response:
            // if (rateLimitExceeded(clientIp)) {
            //     response.setStatus(429);
            //     response.setContentType("application/json");
            //     response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\"}");
            //     return false;
            // }

            return true;
        }

        /**
         * Extracts client IP address considering proxy headers
         */
        private String getClientIpAddress(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
    }

    /**
     * API Versioning Interceptor
     *
     * <p>Handles API version routing and compatibility. Extracts version from
     * API-Version header and makes it available to controllers.</p>
     *
     * <p><strong>Versioning Strategy:</strong></p>
     * <ul>
     *   <li>Reads API-Version header from request</li>
     *   <li>Defaults to v1 if not specified</li>
     *   <li>Stores version in request attribute for controller access</li>
     *   <li>Echoes version back in response header</li>
     * </ul>
     */
    public static class ApiVersioningInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object handler) {
            // Extract API version from header or default to v1
            String apiVersion = request.getHeader("API-Version");
            if (apiVersion == null) {
                apiVersion = "v1"; // Default version
            }

            // Make version available to controllers
            request.setAttribute("apiVersion", apiVersion);

            // Echo version back in response header for client confirmation
            response.setHeader("API-Version", apiVersion);

            log.trace("API version {} requested for {}", apiVersion, request.getRequestURI());
            return true;
        }
    }

    /**
     * Security Headers Filter
     *
     * <p>Filter implementation that adds security headers to all HTTP responses.
     * Complements the SecurityHeadersInterceptor with filter-level processing.</p>
     *
     * <p><strong>Security Headers:</strong></p>
     * <ul>
     *   <li>X-Content-Type-Options: nosniff</li>
     *   <li>X-Frame-Options: DENY</li>
     *   <li>X-XSS-Protection: 1; mode=block</li>
     * </ul>
     */
    public static class SecurityHeadersFilter implements Filter {

        @Override
        public void doFilter(jakarta.servlet.ServletRequest request,
                             jakarta.servlet.ServletResponse response,
                             FilterChain chain) throws IOException, ServletException {

            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // Add essential security headers
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            httpResponse.setHeader("X-Frame-Options", "DENY");
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

            // Continue filter chain
            chain.doFilter(request, response);
        }
    }
}