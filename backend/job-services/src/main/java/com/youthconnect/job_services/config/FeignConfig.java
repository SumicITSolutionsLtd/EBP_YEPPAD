package com.youthconnect.job_services.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign Client Configuration
 *
 * Configures Feign clients for inter-service communication.
 */
@Configuration
public class FeignConfig {

    /**
     * Feign logging level
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;  // Log only method, URL, response status & time
    }

    /**
     * Request interceptor to forward authentication token
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                String authHeader = attributes.getRequest().getHeader("Authorization");
                if (authHeader != null) {
                    requestTemplate.header("Authorization", authHeader);
                }
            }
        };
    }
}