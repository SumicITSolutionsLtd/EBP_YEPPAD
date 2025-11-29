package com.youthconnect.user_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.youthconnect.user_service.security.interceptor.InternalApiInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * WEB CONFIGURATION - FIXED (v4.0 - SWAGGER DOUBLE ENCODING FIX)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * ✅ FIX APPLIED: Removed 'configureMessageConverters'.
 *    This prevents Jackson from serializing the Swagger JSON String,
 *    which was causing the "Unable to render definition" error.
 */
@Slf4j
@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ApplicationProperties applicationProperties;
    private final InternalApiInterceptor internalApiInterceptor;

    // ═══════════════════════════════════════════════════════════════════════
    // ✅ 1. CORS CONFIGURATION
    // ═══════════════════════════════════════════════════════════════════════
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var corsConfig = applicationProperties.getSecurity().getCors();
        log.info("Configuring CORS settings");

        // API Endpoints
        registry.addMapping("/api/**")
                .allowedOriginPatterns(corsConfig.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods(corsConfig.getAllowedMethods().toArray(new String[0]))
                .allowedHeaders(corsConfig.getAllowedHeaders().toArray(new String[0]))
                .allowCredentials(corsConfig.isAllowCredentials())
                .maxAge(corsConfig.getMaxAge());

        // Swagger UI & Actuator
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "HEAD", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ✅ 2. RESOURCE HANDLERS
    // ═══════════════════════════════════════════════════════════════════════
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("Configuring static resources");

        // Swagger UI
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/")
                .resourceChain(false);

        registry.addResourceHandler("/swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/")
                .resourceChain(false);

        // Webjars
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .resourceChain(false);

        // Static content
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ✅ 3. OBJECT MAPPER (JSON Configuration)
    // ═══════════════════════════════════════════════════════════════════════
    @Bean
    public ObjectMapper customObjectMapper() {
        // Spring Boot AUTOMATICALLY detects this bean and configures the default
        // Jackson converter with it. We do NOT need to manually add converters.
        log.info("Configuring custom ObjectMapper");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        if ("development".equals(applicationProperties.getEnvironment())) {
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        }
        return mapper;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ✅ 4. INTERCEPTORS
    // ═══════════════════════════════════════════════════════════════════════
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Performance Monitoring (Skip Swagger)
        registry.addInterceptor(new PerformanceMonitoringInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/**");

        // Security Headers (Skip Swagger)
        registry.addInterceptor(new SecurityHeadersInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html");

        // API Versioning
        registry.addInterceptor(new ApiVersioningInterceptor())
                .addPathPatterns("/api/**");

        // Internal API Auth
        registry.addInterceptor(internalApiInterceptor)
                .addPathPatterns("/api/v1/users/internal/**");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/swagger-ui.html");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ✅ 5. FILTERS
    // ═══════════════════════════════════════════════════════════════════════
    @Bean
    public FilterRegistrationBean<CommonsRequestLoggingFilter> requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setIncludeHeaders(true);
        filter.setMaxPayloadLength(1000);
        FilterRegistrationBean<CommonsRequestLoggingFilter> reg = new FilterRegistrationBean<>(filter);
        reg.addUrlPatterns("/api/*");
        return reg;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ✅ 6. HELPER CLASSES
    // ═══════════════════════════════════════════════════════════════════════

    public static class PerformanceMonitoringInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            request.setAttribute("startTime", System.currentTimeMillis());
            return true;
        }
    }

    public static class SecurityHeadersInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("X-Frame-Options", "DENY");
            response.setHeader("X-XSS-Protection", "1; mode=block");
            return true;
        }
    }

    public static class ApiVersioningInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String version = request.getHeader("API-Version");
            if (version == null) version = "v1";
            response.setHeader("API-Version", version);
            return true;
        }
    }
}