package com.youthconnect.user_service.config;

import com.youthconnect.user_service.security.interceptor.InternalApiInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * WEB MVC CONFIGURATION - Interceptor Registration
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Purpose:
 * Configures Spring MVC components including interceptors for request
 * processing in the user-service microservice.
 *
 * Registered Interceptors:
 * 1. InternalApiInterceptor - Validates API keys for internal endpoints
 *
 * URL Pattern Mapping:
 * - /api/v1/users/internal/** → Requires InternalApiInterceptor validation
 * - All other endpoints → Not intercepted (rely on Spring Security)
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-11-21
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * The internal API interceptor for service-to-service authentication.
     * Injected by Spring from the InternalApiInterceptor @Component.
     */
    private final InternalApiInterceptor internalApiInterceptor;

    /**
     * Registers interceptors with specific URL patterns.
     *
     * This method maps the InternalApiInterceptor to internal API endpoints,
     * ensuring all service-to-service calls are authenticated.
     *
     * @param registry The interceptor registry to add interceptors to
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("Registering InternalApiInterceptor for /api/v1/users/internal/**");

        // Register InternalApiInterceptor for internal endpoints only
        registry.addInterceptor(internalApiInterceptor)
                // Apply to all internal API endpoints
                .addPathPatterns("/api/v1/users/internal/**")
                // Exclude health checks (if any exist under internal path)
                .excludePathPatterns(
                        "/api/v1/users/internal/health",
                        "/api/v1/users/internal/info"
                );

        log.info("InternalApiInterceptor registered successfully");
    }
}