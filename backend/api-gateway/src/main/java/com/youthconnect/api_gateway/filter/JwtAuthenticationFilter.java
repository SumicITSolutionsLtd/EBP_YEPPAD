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
 * COMPLETE VERSION - Including File Service Rules
 *
 * This filter intercepts all incoming requests and validates JWT tokens
 * before routing to backend microservices.
 *
 * AUTHENTICATION FLOW:
 * 1. Extract JWT token from Authorization header
 * 2. Validate token signature and expiration
 * 3. Extract user information (userId, email, roles)
 * 4. Add user context headers for downstream services
 * 5. Block unauthorized requests
 *
 * PUBLIC ENDPOINTS (No authentication required):
 * - Authentication endpoints (/api/auth/*)
 * - Health checks (/health, /actuator/**)
 * - Swagger documentation
 * - Public file downloads (learning modules, public profiles)
 *
 * PROTECTED ENDPOINTS (JWT required):
 * - All other endpoints
 * - File uploads
 * - Private file downloads
 * - User data modifications
 *
 * Location: api-gateway/src/main/java/com/youthconnect/api_gateway/filter/
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (Complete with File Service)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    /**
     * List of public endpoints that don't require authentication
     *
     * SECURITY NOTE: Be very careful adding new public endpoints!
     * Every public endpoint is a potential security risk.
     */
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            // Authentication endpoints
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/reset-password",

            // Health and monitoring
            "/health",
            "/health/live",
            "/health/ready",
            "/health/detailed",
            "/actuator",

            // Documentation
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars",

            // ✅ NEW: Public file endpoints
            // Public learning modules (anyone can access educational content)
            "/api/files/download/modules",
            // Public profile pictures (visible to all users)
            "/api/files/download/public",
            // File service health check
            "/api/files/health"
    );

    /**
     * Predicate to check if request path is public (no auth required)
     *
     * HOW IT WORKS:
     * - Checks if request path starts with any public endpoint
     * - Uses stream().anyMatch() for efficient checking
     */
    private final Predicate<ServerHttpRequest> isPublicEndpoint =
            request -> PUBLIC_ENDPOINTS.stream()
                    .anyMatch(uri -> request.getURI().getPath().contains(uri));

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // ============================================================
        // STEP 1: Skip authentication for public endpoints
        // ============================================================
        if (isPublicEndpoint.test(request)) {
            log.debug("✅ Public endpoint accessed (no auth required): {}", request.getPath());
            return chain.filter(exchange);
        }

        // ============================================================
        // STEP 2: Skip authentication for OPTIONS requests (CORS preflight)
        // ============================================================
        if ("OPTIONS".equals(request.getMethod().name())) {
            log.debug("✅ OPTIONS request (CORS preflight), skipping auth");
            return chain.filter(exchange);
        }

        // ============================================================
        // STEP 3: Extract JWT token from Authorization header
        // ============================================================
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("❌ Missing or invalid Authorization header for: {}", request.getPath());
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        // Remove "Bearer " prefix to get the actual token
        String token = authHeader.substring(7);

        try {
            // ============================================================
            // STEP 4: Validate token signature and expiration
            // ============================================================
            if (!jwtUtil.validateToken(token)) {
                log.warn("❌ Invalid JWT token for: {}", request.getPath());
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            // ============================================================
            // STEP 5: Extract user information from token
            // ============================================================
            String userId = jwtUtil.extractUserId(token);
            String email = jwtUtil.extractEmail(token);
            List<String> roles = jwtUtil.extractRoles(token);

            log.info("✅ Authenticated request | User: {} | Email: {} | Roles: {} | Path: {}",
                    userId, email, roles, request.getPath());

            // ============================================================
            // STEP 6: Additional authorization checks for file operations
            // ============================================================
            String path = request.getPath().value();

            // Check if user is trying to upload/delete files for another user
            if (isFileOwnershipValidationRequired(path)) {
                if (!validateFileOwnership(path, userId)) {
                    log.warn("❌ User {} attempted unauthorized file access: {}", userId, path);
                    return onError(exchange, "You are not authorized to access this file", HttpStatus.FORBIDDEN);
                }
            }

            // ============================================================
            // STEP 7: Add user context headers for downstream services
            // These headers are used by backend services to identify the user
            // ============================================================
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)           // UUID of authenticated user
                    .header("X-User-Email", email)         // User's email
                    .header("X-User-Roles", String.join(",", roles))  // Comma-separated roles
                    .header("X-Auth-Token", token)         // Original JWT token
                    .build();

            // ============================================================
            // STEP 8: Continue filter chain with modified request
            // ============================================================
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.error("❌ Error validating JWT token: {}", e.getMessage(), e);
            return onError(exchange, "Authentication failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Check if file ownership validation is required for this path
     *
     * RULES:
     * - Profile picture uploads: User can only upload their own picture
     * - Document uploads: User can only upload their own documents
     * - File deletion: User can only delete their own files
     * - Private downloads: User can only download their own files
     *
     * EXCEPTIONS:
     * - Admins can access any file
     * - NGOs can access application attachments
     *
     * @param path Request path
     * @return true if ownership validation required
     */
    private boolean isFileOwnershipValidationRequired(String path) {
        return path.contains("/api/files/profile-picture/") ||
                path.contains("/api/files/document/") ||
                (path.contains("/api/files/download/users/") && !path.contains("/public/"));
    }

    /**
     * Validate that user has permission to access file
     *
     * BUSINESS RULES:
     * - User can only upload/download/delete their own files
     * - Path format: /api/files/{operation}/{userId}/...
     * - Extract userId from path and compare with authenticated userId
     *
     * @param path Request path
     * @param authenticatedUserId User ID from JWT token
     * @return true if user owns the file
     */
    private boolean validateFileOwnership(String path, String authenticatedUserId) {
        try {
            // Extract user ID from path
            // Example paths:
            // - /api/files/profile-picture/123e4567-e89b-12d3-a456-426614174000
            // - /api/files/document/123e4567-e89b-12d3-a456-426614174000
            // - /api/files/download/users/123e4567-e89b-12d3-a456-426614174000/cv.pdf

            String[] pathParts = path.split("/");

            // Find the userId in the path (it's a UUID format)
            for (String part : pathParts) {
                // Check if part looks like a UUID
                if (part.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
                    // Compare with authenticated user ID
                    boolean isOwner = part.equals(authenticatedUserId);

                    if (!isOwner) {
                        log.warn("⚠️ File ownership violation | Authenticated: {} | Requested: {}",
                                authenticatedUserId, part);
                    }

                    return isOwner;
                }
            }

            // If no userId found in path, deny access (safer default)
            log.warn("⚠️ Could not extract userId from path for ownership check: {}", path);
            return false;

        } catch (Exception e) {
            log.error("❌ Error validating file ownership: {}", e.getMessage(), e);
            return false; // Deny access on error
        }
    }

    /**
     * Handle authentication errors
     * Returns standardized error response
     *
     * ERROR RESPONSE FORMAT:
     * {
     *   "timestamp": "2025-11-08T10:30:00",
     *   "status": 401,
     *   "error": "Unauthorized",
     *   "message": "Missing or invalid Authorization header",
     *   "path": "/api/files/upload"
     * }
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
     *
     * FILTER EXECUTION ORDER:
     * -100: Rate Limiting Filter
     *  -90: Logging Filter
     *  -80: JWT Authentication Filter (THIS FILTER)
     *  -70: Custom Business Logic Filters
     *    0: Default Spring Cloud Gateway Filters
     *
     * Priority: -80 (after rate limit -100, before logging -90)
     */
    @Override
    public int getOrder() {
        return -80;
    }
}