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
 * Web Configuration for Entrepreneurship booster Platform Uganda User Service
 *
 * Comprehensive web configuration handling HTTP request/response processing:
 * - CORS for frontend integration
 * - JSON serialization with Uganda timezone
 * - Request/response logging and monitoring
 * - Security headers injection
 * - Static resource serving
 * - Performance interceptors
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 - FIXED CORS mapping issue
 */
@Slf4j
@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ApplicationProperties applicationProperties;

    /**
     * CORS Configuration - FIXED: Separate addMapping calls
     *
     * Configures Cross-Origin Resource Sharing for frontend integration.
     * Each endpoint pattern requires its own addMapping() call.
     *
     * Security: Only whitelisted origins, specific methods, credentials optional
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var corsConfig = applicationProperties.getSecurity().getCors();

        log.info("Configuring CORS for {} origins", corsConfig.getAllowedOrigins().size());

        // Main API CORS - authenticated requests
        registry.addMapping("/api/**")
                .allowedOriginPatterns(corsConfig.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods(corsConfig.getAllowedMethods().toArray(new String[0]))
                .allowedHeaders(corsConfig.getAllowedHeaders().toArray(new String[0]))
                .exposedHeaders(corsConfig.getExposedHeaders().toArray(new String[0]))
                .allowCredentials(corsConfig.isAllowCredentials())
                .maxAge(corsConfig.getMaxAge());

        // Actuator endpoints - monitoring tools only
        registry.addMapping("/actuator/**")
                .allowedOriginPatterns("http://localhost:*", "https://*.youthconnect.ug")
                .allowedMethods("GET", "POST")
                .allowCredentials(false)
                .maxAge(corsConfig.getMaxAge());

        // FIXED: Separate calls for Swagger UI and API docs
        registry.addMapping("/swagger-ui/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET")
                .allowCredentials(false)
                .maxAge(corsConfig.getMaxAge());

        registry.addMapping("/v3/api-docs/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET")
                .allowCredentials(false)
                .maxAge(corsConfig.getMaxAge());

        log.debug("CORS configuration applied successfully");
    }

    /**
     * Static Resource Handlers - File serving with caching
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        var uploadConfig = applicationProperties.getUpload();

        log.info("Configuring resource handlers - upload dir: {}", uploadConfig.getUploadDir());

        // Swagger UI - no cache in dev
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")
                .setCachePeriod("development".equals(applicationProperties.getEnvironment()) ? 0 : 3600)
                .resourceChain(true);

        // Static assets - 1 year cache
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(31556926)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());

        // User uploads - 1 day cache
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadConfig.getUploadDir() + "/")
                .setCachePeriod(86400)
                .resourceChain(false);

        // API docs
        registry.addResourceHandler("/docs/**")
                .addResourceLocations("classpath:/static/docs/")
                .setCachePeriod(3600);

        log.debug("Resource handlers configured");
    }

    /**
     * Custom ObjectMapper - Uganda timezone, ISO-8601 dates
     */
    @Bean
    public ObjectMapper customObjectMapper() {
        log.info("Configuring custom ObjectMapper");

        ObjectMapper mapper = new ObjectMapper();

        // Java 8 date/time support
        mapper.registerModule(new JavaTimeModule());

        // ISO-8601 dates with timezone
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);

        // API evolution support
        mapper.configure(
                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false
        );
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // Pretty print in dev
        if ("development".equals(applicationProperties.getEnvironment())) {
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        }

        log.debug("ObjectMapper configured");
        return mapper;
    }

    /**
     * HTTP Message Converters - JSON with custom ObjectMapper
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.debug("Configuring message converters");

        MappingJackson2HttpMessageConverter jsonConverter =
                new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(customObjectMapper());
        jsonConverter.setPrettyPrint("development".equals(applicationProperties.getEnvironment()));

        converters.add(0, jsonConverter);
        log.debug("JSON converter configured");
    }

    /**
     * Content Negotiation - JSON default, supports XML/CSV
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        log.debug("Configuring content negotiation");

        configurer
                .favorParameter(false)
                .favorPathExtension(false)
                .ignoreAcceptHeader(false)
                .defaultContentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .mediaType("json", org.springframework.http.MediaType.APPLICATION_JSON)
                .mediaType("xml", org.springframework.http.MediaType.APPLICATION_XML)
                .mediaType("csv", org.springframework.http.MediaType.parseMediaType("text/csv"));
    }

    /**
     * Request Interceptors - Performance, security, rate limiting
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("Configuring request interceptors");

        // Performance monitoring
        registry.addInterceptor(new PerformanceMonitoringInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/health", "/actuator/**");

        // Security headers
        registry.addInterceptor(new SecurityHeadersInterceptor())
                .addPathPatterns("/**");

        // Rate limiting (if enabled)
        if (applicationProperties.getSecurity().getRateLimit().isEnabled()) {
            registry.addInterceptor(new RateLimitingInterceptor())
                    .addPathPatterns("/api/**")
                    .excludePathPatterns("/api/auth/health");
        }

        // API versioning
        registry.addInterceptor(new ApiVersioningInterceptor())
                .addPathPatterns("/api/**");

        log.debug("Configured 4 interceptors");
    }

    /**
     * View Controllers - Convenience redirects
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        log.debug("Configuring view controllers");

        registry.addRedirectViewController("/", "/swagger-ui.html");
        registry.addRedirectViewController("/docs", "/swagger-ui.html");
        registry.addRedirectViewController("/api", "/swagger-ui.html");
        registry.addRedirectViewController("/health", "/actuator/health");
        registry.addRedirectViewController("/status", "/actuator/health");
    }

    /**
     * Request Logging Filter - FIXED: Single-argument constructor
     */
    @Bean
    public FilterRegistrationBean<CommonsRequestLoggingFilter> requestLoggingFilter() {
        log.info("Configuring request logging filter");

        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(applicationProperties.getAudit().isIncludeRequestBody());
        filter.setIncludeHeaders(true);
        filter.setIncludeClientInfo(true);
        filter.setMaxPayloadLength(1000);

        // FIXED: Use single-argument constructor
        FilterRegistrationBean<CommonsRequestLoggingFilter> registration =
                new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);

        return registration;
    }

    /**
     * Security Headers Filter - FIXED: Single-argument constructor
     */
    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        log.info("Configuring security headers filter");

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
     * Tracks request duration and logs slow requests
     */
    public static class PerformanceMonitoringInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object handler) {
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
            Long startTime = (Long) request.getAttribute("startTime");
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                String method = request.getMethod();
                String uri = request.getRequestURI();
                int status = response.getStatus();

                log.info("Request completed: {} {} - Status: {} - Duration: {}ms",
                        method, uri, status, duration);

                if (duration > 5000) {
                    log.warn("Slow request: {} {} took {}ms", method, uri, duration);
                }
            }

            if (ex != null) {
                log.error("Request failed: {}", ex.getMessage());
            }
        }

        private String getClientIpAddress(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty() &&
                    !"unknown".equalsIgnoreCase(xForwardedFor)) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIP = request.getHeader("X-Real-IP");
            if (xRealIP != null && !xRealIP.isEmpty() &&
                    !"unknown".equalsIgnoreCase(xRealIP)) {
                return xRealIP;
            }

            return request.getRemoteAddr();
        }
    }

    /**
     * Security Headers Interceptor
     * Adds headers to protect against XSS, clickjacking, etc.
     */
    public static class SecurityHeadersInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object handler) {
            // Content Security Policy
            response.setHeader("Content-Security-Policy",
                    "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'");

            // Clickjacking protection
            response.setHeader("X-Frame-Options", "DENY");

            // XSS protection
            response.setHeader("X-XSS-Protection", "1; mode=block");

            // MIME sniffing protection
            response.setHeader("X-Content-Type-Options", "nosniff");

            // Referrer policy
            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

            // HSTS for HTTPS
            if (request.isSecure()) {
                response.setHeader("Strict-Transport-Security",
                        "max-age=31536000; includeSubDomains");
            }

            return true;
        }
    }

    /**
     * Rate Limiting Interceptor (Placeholder)
     * TODO: Implement Redis-based rate limiting for production
     */
    public static class RateLimitingInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object handler) throws IOException {
            String clientIp = getClientIpAddress(request);
            log.debug("Rate limit check for IP: {}", clientIp);

            // TODO: Implement actual rate limiting with Redis
            return true;
        }

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
     * Handles version routing via API-Version header
     */
    public static class ApiVersioningInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object handler) {
            String apiVersion = request.getHeader("API-Version");
            if (apiVersion == null) {
                apiVersion = "v1";
            }

            request.setAttribute("apiVersion", apiVersion);
            response.setHeader("API-Version", apiVersion);

            log.trace("API version {} for {}", apiVersion, request.getRequestURI());
            return true;
        }
    }

    /**
     * Security Headers Filter
     * Filter-level security header injection
     */
    public static class SecurityHeadersFilter implements Filter {

        @Override
        public void doFilter(jakarta.servlet.ServletRequest request,
                             jakarta.servlet.ServletResponse response,
                             FilterChain chain) throws IOException, ServletException {

            HttpServletResponse httpResponse = (HttpServletResponse) response;

            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
            httpResponse.setHeader("X-Frame-Options", "DENY");
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

            chain.doFilter(request, response);
        }
    }
}