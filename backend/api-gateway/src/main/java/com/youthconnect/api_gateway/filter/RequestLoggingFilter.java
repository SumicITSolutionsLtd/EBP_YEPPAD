package com.youthconnect.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Request Logging Filter
 *
 * Logs all incoming requests with:
 * - Request ID (for tracing across services)
 * - HTTP method and path
 * - Client IP address
 * - Request timestamp
 * - Response time
 *
 * This is essential for:
 * - Debugging issues in production
 * - Performance monitoring
 * - Security audit trails
 * - Request tracing across microservices
 *
 * Location: api-gateway/src/main/java/com/youthconnect/api_gateway/filter/
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Generate or retrieve request ID for tracing
        String requestId = request.getHeaders().getFirst(REQUEST_ID_HEADER);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }

        // FIXED: Make requestId effectively final for lambda
        final String finalRequestId = requestId;

        // Add request ID to response headers for client tracking
        exchange.getResponse().getHeaders().add(REQUEST_ID_HEADER, finalRequestId);

        // Store start time for response time calculation
        exchange.getAttributes().put(START_TIME_ATTRIBUTE, System.currentTimeMillis());

        // Extract client IP
        String clientIp = getClientIp(request);

        // Log incoming request
        log.info(">>> Incoming Request | ID: {} | Method: {} | Path: {} | IP: {}",
                finalRequestId,
                request.getMethod(),
                request.getPath(),
                clientIp);

        // FIXED: Create final reference for request path for lambda
        final String requestPath = request.getPath().value();

        // Continue filter chain and log response
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Long startTime = exchange.getAttribute(START_TIME_ATTRIBUTE);
            if (startTime != null) {
                long responseTime = System.currentTimeMillis() - startTime;
                int statusCode = exchange.getResponse().getStatusCode() != null
                        ? exchange.getResponse().getStatusCode().value()
                        : 0;

                log.info("<<< Outgoing Response | ID: {} | Status: {} | Time: {}ms",
                        finalRequestId,
                        statusCode,
                        responseTime);

                // Warn if request is slow (> 3 seconds)
                if (responseTime > 3000) {
                    log.warn("SLOW REQUEST DETECTED | ID: {} | Path: {} | Time: {}ms",
                            finalRequestId, requestPath, responseTime);
                }
            }
        }));
    }

    /**
     * Extract client IP address from request
     * Checks X-Forwarded-For header first (for proxied requests)
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }

    /**
     * Run this filter early (but after rate limiting)
     */
    @Override
    public int getOrder() {
        return -90; // High priority, but after rate limiting (-100)
    }
}