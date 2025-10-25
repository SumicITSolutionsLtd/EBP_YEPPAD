package com.youthconnect.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Security Headers Filter
 *
 * Adds security headers to all outgoing responses to protect against
 * common web vulnerabilities:
 *
 * - X-Content-Type-Options: Prevents MIME-sniffing attacks
 * - X-Frame-Options: Prevents clickjacking attacks
 * - X-XSS-Protection: Enables browser XSS filtering
 * - Strict-Transport-Security: Forces HTTPS connections
 * - Content-Security-Policy: Prevents XSS and injection attacks
 *
 * These headers are critical for production security compliance.
 *
 * Location: api-gateway/src/main/java/com/youthconnect/api_gateway/filter/
 */
@Slf4j
@Component
public class SecurityHeadersFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();

            // Prevent MIME-sniffing attacks
            response.getHeaders().add("X-Content-Type-Options", "nosniff");

            // Prevent clickjacking attacks
            response.getHeaders().add("X-Frame-Options", "DENY");

            // Enable browser XSS protection
            response.getHeaders().add("X-XSS-Protection", "1; mode=block");

            // Force HTTPS connections (only in production)
            // Disabled in development to allow HTTP on localhost
            // Uncomment for production:
            // response.getHeaders().add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

            // Content Security Policy - prevents XSS attacks
            // Adjust based on your frontend needs
            response.getHeaders().add("Content-Security-Policy",
                    "default-src 'self'; " +
                            "script-src 'self' 'unsafe-inline'; " +
                            "style-src 'self' 'unsafe-inline'; " +
                            "img-src 'self' data: https:; " +
                            "font-src 'self' data:; " +
                            "connect-src 'self' https:");

            // Remove server information header for security
            response.getHeaders().remove("Server");

            log.debug("Security headers added to response for: {}",
                    exchange.getRequest().getPath());
        }));
    }

    /**
     * Run after other filters but before sending response
     */
    @Override
    public int getOrder() {
        return -50; // Medium priority
    }
}