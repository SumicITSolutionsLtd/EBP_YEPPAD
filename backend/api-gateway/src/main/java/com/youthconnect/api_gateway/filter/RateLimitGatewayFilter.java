package com.youthconnect.api_gateway.filter;

import com.youthconnect.api_gateway.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global Rate Limiting Filter for API Gateway
 *
 * Intercepts ALL incoming requests and applies rate limiting based on:
 * - Client IP address
 * - Endpoint type (auth, USSD, general)
 *
 * This filter runs BEFORE routing to prevent overloading backend services.
 *
 * Location: api-gateway/src/main/java/com/youthconnect/api_gateway/filter/
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitGatewayFilter implements GlobalFilter, Ordered {

    private final RateLimitConfig rateLimitConfig;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Skip rate limiting if disabled (useful for testing)
        if (!rateLimitConfig.isRateLimitEnabled()) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // Extract client IP address
        String clientIp = getClientIp(request);

        // Determine endpoint type based on request path
        RateLimitConfig.EndpointType endpointType = determineEndpointType(request);

        // Get or create rate limit bucket for this client
        Bucket bucket = rateLimitConfig.resolveBucket(clientIp, endpointType);

        // Try to consume 1 token from the bucket
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Request allowed - add rate limit headers
            response.getHeaders().add("X-Rate-Limit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));

            log.debug("Request allowed for IP: {} | Type: {} | Remaining: {}",
                    clientIp, endpointType, probe.getRemainingTokens());

            return chain.filter(exchange);
        } else {
            // Rate limit exceeded - reject request
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;

            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            response.getHeaders().add("X-Rate-Limit-Retry-After-Seconds",
                    String.valueOf(waitForRefill));

            log.warn("Rate limit exceeded for IP: {} | Type: {} | Retry after: {} seconds",
                    clientIp, endpointType, waitForRefill);

            return response.setComplete();
        }
    }

    /**
     * Extract client IP address from request
     * Checks X-Forwarded-For header first (for proxied requests)
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }

        // Fallback to remote address
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    /**
     * Determine endpoint type based on request path
     * This determines which rate limit strategy to apply
     */
    private RateLimitConfig.EndpointType determineEndpointType(ServerHttpRequest request) {
        String path = request.getPath().toString();

        // Authentication endpoints get strictest limits
        if (path.startsWith("/api/auth/")) {
            return RateLimitConfig.EndpointType.AUTH;
        }

        // USSD endpoints get moderate limits
        if (path.startsWith("/api/ussd/")) {
            return RateLimitConfig.EndpointType.USSD;
        }

        // All other endpoints get general limits
        return RateLimitConfig.EndpointType.GENERAL;
    }

    /**
     * Run this filter early in the chain (before routing)
     * Lower order value = higher priority
     */
    @Override
    public int getOrder() {
        return -100; // High priority
    }
}