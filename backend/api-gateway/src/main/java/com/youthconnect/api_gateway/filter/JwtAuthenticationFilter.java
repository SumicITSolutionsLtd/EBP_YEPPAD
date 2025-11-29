package com.youthconnect.api_gateway.filter;

import com.youthconnect.api_gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    /**
     * 🔓 PUBLIC ENDPOINTS WHITELIST
     */
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            // Auth
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/reset-password",

            // Health
            "/health",
            "/actuator",

            // ✅ FIXED: Allow ALL Swagger resources
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars",
            "/configuration/ui",
            "/configuration/security",

            // ✅ FIXED: Allow Service-specific Swagger Paths
            "/auth-service/swagger-ui",
            "/auth-service/v3/api-docs",
            "/user-service/swagger-ui",
            "/user-service/v3/api-docs",
            "/job-services/swagger-ui",
            "/job-services/v3/api-docs"
    );

    private final Predicate<ServerHttpRequest> isPublicEndpoint = request -> {
        String path = request.getURI().getPath();
        // Allow if path matches whitelist OR ends with extensions used by Swagger
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::contains) ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(".png") ||
                path.endsWith(".html");
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 1. Allow Public Endpoints
        if (isPublicEndpoint.test(request)) {
            return chain.filter(exchange);
        }

        // 2. Allow OPTIONS (CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod().name())) {
            return chain.filter(exchange);
        }

        // 3. Check Authorization Header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        // 4. Validate Token
        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            // 5. Extract Identity & Forward
            String userId = jwtUtil.extractUserId(token);
            String email = jwtUtil.extractEmail(token);
            List<String> roles = jwtUtil.extractRoles(token);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Email", email)
                    .header("X-User-Roles", String.join(",", roles))
                    .header("X-Auth-Token", token)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("Auth Error: {}", e.getMessage());
            return onError(exchange, "Authentication failed", HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String errorJson = String.format("{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                LocalDateTime.now(), status.value(), status.getReasonPhrase(), message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(errorJson.getBytes())));
    }

    @Override
    public int getOrder() {
        return -80;
    }
}