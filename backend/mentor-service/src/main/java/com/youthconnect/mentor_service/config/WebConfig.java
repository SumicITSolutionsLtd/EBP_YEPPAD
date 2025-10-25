package com.youthconnect.mentor_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.TimeZone;

/**
 * ============================================================================
 * WEB MVC CONFIGURATION
 * ============================================================================
 *
 * Configures Spring MVC components for HTTP request/response handling.
 *
 * CONFIGURATION FEATURES:
 * - CORS (Cross-Origin Resource Sharing)
 * - Request/response interceptors
 * - JSON serialization/deserialization
 * - Timezone handling
 * - Request logging
 *
 * CORS CONFIGURATION:
 * - Allows requests from web and mobile applications
 * - Configurable allowed origins, methods, and headers
 * - Credentials support for cookies/authentication
 *
 * JSON CONFIGURATION:
 * - Java 8 Date/Time API support (LocalDateTime, Instant, etc.)
 * - Timezone: Africa/Kampala (EAT - UTC+3)
 * - Pretty printing disabled in production
 * - Null value handling
 *
 * INTERCEPTORS:
 * - Request logging interceptor
 * - Performance monitoring interceptor
 * - User context injection
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-21
 * ============================================================================
 */
@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configure CORS Mappings
     * Allows cross-origin requests from web and mobile clients
     *
     * CORS POLICY:
     * - Allowed origins: Web app, mobile app, admin panel
     * - Allowed methods: GET, POST, PUT, DELETE, PATCH
     * - Allowed headers: All headers
     * - Credentials: Allowed (for cookies/auth tokens)
     * - Max age: 3600 seconds (1 hour)
     *
     * SECURITY NOTE:
     * - CORS is also configured at API Gateway level
     * - This is a fallback configuration
     * - Production should use specific origins, not wildcards
     *
     * @param registry CORS registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("Configuring CORS mappings");

        registry.addMapping("/api/**")
                // Allowed origins (should be externalized to properties)
                .allowedOrigins(
                        "http://localhost:3000",      // React dev server
                        "http://localhost:3001",      // Mobile dev server
                        "https://platform.ug",        // Production web app
                        "https://admin.platform.ug"   // Admin panel
                )
                // Allowed HTTP methods
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                // Allow all headers
                .allowedHeaders("*")
                // Allow credentials (cookies, authorization headers)
                .allowCredentials(true)
                // Cache preflight requests for 1 hour
                .maxAge(3600);

        log.info("CORS configuration completed");
    }

    /**
     * Configure Request Interceptors
     * Adds interceptors to the request processing chain
     *
     * INTERCEPTORS:
     * 1. LoggingInterceptor - Logs all incoming requests
     * 2. PerformanceInterceptor - Measures request execution time
     * 3. UserContextInterceptor - Extracts user info from headers
     *
     * @param registry Interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("Configuring request interceptors");

        // Add logging interceptor (logs all requests)
        registry.addInterceptor(new LoggingInterceptor())
                .addPathPatterns("/api/**")
                .order(1);

        // Add performance monitoring interceptor
        registry.addInterceptor(new PerformanceInterceptor())
                .addPathPatterns("/api/**")
                .order(2);

        // Add user context interceptor (extracts user from headers)
        registry.addInterceptor(new UserContextInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/public/**")
                .order(3);

        log.info("Request interceptors configured");
    }

    /**
     * Configure Message Converters
     * Customizes JSON serialization/deserialization
     *
     * JSON CONFIGURATION:
     * - JavaTimeModule: Support for Java 8 Date/Time API
     * - Timezone: Africa/Kampala (EAT)
     * - Write dates as ISO-8601 strings
     * - Disable timestamp format
     * - Include null values in response
     *
     * @param converters List of message converters
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("Configuring JSON message converters");

        // Find existing Jackson converter
        converters.stream()
                .filter(converter -> converter instanceof MappingJackson2HttpMessageConverter)
                .map(converter -> (MappingJackson2HttpMessageConverter) converter)
                .forEach(converter -> {
                    ObjectMapper objectMapper = converter.getObjectMapper();

                    // Register Java 8 Date/Time module
                    objectMapper.registerModule(new JavaTimeModule());

                    // Set timezone to East Africa Time (UTC+3)
                    objectMapper.setTimeZone(TimeZone.getTimeZone("Africa/Kampala"));

                    // Write dates as ISO-8601 strings (not timestamps)
                    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                    // Don't include null values in JSON (reduces response size)
                    // objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

                    log.info("JSON message converter configured with EAT timezone");
                });
    }

    /**
     * Object Mapper Bean
     * Global ObjectMapper for JSON processing
     *
     * FEATURES:
     * - Java 8 Date/Time API support
     * - East Africa Time timezone
     * - ISO-8601 date format
     * - Consistent across application
     *
     * @return Configured ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Register Java 8 Date/Time module
        objectMapper.registerModule(new JavaTimeModule());

        // Set timezone to East Africa Time
        objectMapper.setTimeZone(TimeZone.getTimeZone("Africa/Kampala"));

        // Write dates as ISO-8601 strings
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Pretty print in development (should be disabled in production)
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        log.info("Global ObjectMapper bean configured");
        return objectMapper;
    }
}

/**
 * Logging Interceptor
 * Logs all incoming HTTP requests
 */
@Slf4j
class LoggingInterceptor implements org.springframework.web.servlet.HandlerInterceptor {

    @Override
    public boolean preHandle(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            Object handler
    ) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String remoteAddr = request.getRemoteAddr();

        log.info("Incoming Request: {} {} from {}", method, uri, remoteAddr);
        return true;
    }

    @Override
    public void afterCompletion(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        int status = response.getStatus();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        if (status >= 400) {
            log.warn("Request completed: {} {} - Status: {}", method, uri, status);
        } else {
            log.info("Request completed: {} {} - Status: {}", method, uri, status);
        }
    }
}

/**
 * Performance Interceptor
 * Measures request execution time
 */
@Slf4j
class PerformanceInterceptor implements org.springframework.web.servlet.HandlerInterceptor {

    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            Object handler
    ) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime != null) {
            long executionTime = System.currentTimeMillis() - startTime;
            String method = request.getMethod();
            String uri = request.getRequestURI();

            if (executionTime > 1000) {
                log.warn("SLOW REQUEST: {} {} took {}ms", method, uri, executionTime);
            } else {
                log.debug("Request execution time: {} {} - {}ms", method, uri, executionTime);
            }
        }
    }
}

/**
 * User Context Interceptor
 * Extracts user information from request headers
 */
@Slf4j
class UserContextInterceptor implements org.springframework.web.servlet.HandlerInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String USER_EMAIL_HEADER = "X-User-Email";

    @Override
    public boolean preHandle(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            Object handler
    ) {
        // Extract user information from headers (set by API Gateway)
        String userId = request.getHeader(USER_ID_HEADER);
        String userRole = request.getHeader(USER_ROLE_HEADER);
        String userEmail = request.getHeader(USER_EMAIL_HEADER);

        if (userId != null) {
            // Store user context in request attributes
            request.setAttribute("userId", Long.parseLong(userId));
            request.setAttribute("userRole", userRole);
            request.setAttribute("userEmail", userEmail);

            log.debug("User context: userId={}, role={}, email={}",
                    userId, userRole, userEmail);
        } else {
            log.warn("No user context found in request headers for: {} {}",
                    request.getMethod(), request.getRequestURI());
        }

        return true;
    }
}