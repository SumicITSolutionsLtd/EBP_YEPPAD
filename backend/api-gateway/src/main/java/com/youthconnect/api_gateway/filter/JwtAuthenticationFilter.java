package com.youthconnect.api_gateway.filter;

import com.youthconnect.api_gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

/**
 * JWT Authentication Filter for API Gateway
 *
 * This filter intercepts all incoming requests and validates JWT tokens
 * before routing to backend microservices.
 *
 * Responsibilities:
 * - Extract JWT token from Authorization header
 * - Validate token signature and expiration
 * - Extract user information (userId, email, roles)
 * - Add user context headers for downstream services
 * - Block unauthorized requests
 *
 * Public endpoints (no authentication required):
 * - /api/auth/register
 * - /api/auth/login
 * - /api/auth/refresh
 * - /health, /actuator/**
 * - Swagger documentation endpoints
 *
 * All other endpoints require valid JWT token.
 *
 * Location: api-gateway/src/main/java/com/youthconnect/api_gateway/filter/
 *
 * @author Youth Connect Development Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    /**
     * List of public endpoints that don't require authentication
     */
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/reset-password",
            "/health",
            "/health/live",
            "/health/ready",
            "/health/detailed",
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars"
    );

    /**
     * Predicate to check if request path is public (no auth required)
     */
    private final Predicate<ServerHttpRequest> isPublicEndpoint =
            request -> PUBLIC_ENDPOINTS.stream()
                    .anyMatch(uri -> request.getURI().getPath().contains(uri));

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // Skip authentication for public endpoints
        if (isPublicEndpoint.test(request)) {
            log.debug("Public endpoint accessed: {}", request.getPath());
            return chain.filter(exchange);
        }

        // Skip authentication for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equals(request.getMethod().name())) {
            return chain.filter(exchange);
        }

        // Extract JWT token from Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for: {}", request.getPath());
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            // Validate token signature and expiration
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid JWT token for: {}", request.getPath());
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            // Extract user information from token
            String userId = jwtUtil.extractUserId(token);
            String email = jwtUtil.extractEmail(token);
            List<String> roles = jwtUtil.extractRoles(token);

            log.info("Authenticated request | User: {} | Email: {} | Roles: {} | Path: {}",
                    userId, email, roles, request.getPath());

            // Add user context headers for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Email", email)
                    .header("X-User-Roles", String.join(",", roles))
                    .header("X-Auth-Token", token)
                    .build();

            // Continue filter chain with modified request
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("Error validating JWT token: {}", e.getMessage(), e);
            return onError(exchange, "Authentication failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Handle authentication errors
     * Returns standardized error response
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

        String errorJson = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                java.time.LocalDateTime.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value()
        );

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(errorJson.getBytes()))
        );
    }

    /**
     * Run this filter after rate limiting but before routing
     * Priority: -80 (after rate limit -100, before logging -90)
     */
    @Override
    public int getOrder() {
        return -80;
    }
}