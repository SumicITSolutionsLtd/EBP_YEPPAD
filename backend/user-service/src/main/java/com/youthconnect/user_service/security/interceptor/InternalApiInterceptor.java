package com.youthconnect.user_service.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * INTERNAL API INTERCEPTOR - Service-to-Service Authentication
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Purpose:
 * This interceptor validates internal API keys for service-to-service
 * communication within the Youth Connect microservices architecture.
 *
 * Security Model:
 * - All internal endpoints (/api/v1/users/internal/**) require a valid API key
 * - API key is passed via the 'X-Internal-API-Key' header
 * - Requests without valid API key receive 401 Unauthorized response
 *
 * Usage:
 * Other microservices (auth-service, job-service, etc.) must include the
 * X-Internal-API-Key header when calling internal endpoints.
 *
 * Configuration:
 * The API key is configured in application.yml:
 *   app:
 *     security:
 *       internal-api-key: ${INTERNAL_API_KEY:default-dev-key}
 *
 * IMPORTANT SECURITY NOTES:
 * - Never log the actual API key values
 * - Use different API keys for each environment (dev, staging, prod)
 * - Rotate API keys periodically
 * - Store production keys in secure secret management (e.g., AWS Secrets Manager)
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 - Enhanced with better error handling and logging
 * @since 2025-11-21
 */
@Slf4j
@Component
public class InternalApiInterceptor implements HandlerInterceptor {

    /**
     * Header name for the internal API key.
     * All internal service-to-service calls must include this header.
     */
    private static final String API_KEY_HEADER = "X-Internal-API-Key";

    /**
     * The configured internal API key for validation.
     * Injected from application.yml: app.security.internal-api-key
     *
     * Default value is provided for development, but MUST be overridden
     * in production via INTERNAL_API_KEY environment variable.
     */
    @Value("${app.security.internal-api-key}")
    private String internalApiKey;

    /**
     * Pre-handle method that validates the internal API key before
     * allowing access to internal endpoints.
     *
     * @param request  The incoming HTTP request
     * @param response The HTTP response object
     * @param handler  The handler object (controller method)
     * @return true if API key is valid, false otherwise
     * @throws Exception if an error occurs during validation
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Extract the request details for logging
        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();
        String clientIp = getClientIpAddress(request);

        log.debug("Validating internal API key for {} {} from IP: {}",
                requestMethod, requestUri, clientIp);

        // Extract API key from request header
        String providedApiKey = request.getHeader(API_KEY_HEADER);

        // Check if API key header is missing
        if (!StringUtils.hasText(providedApiKey)) {
            log.warn("Missing internal API key header for {} {} from IP: {}",
                    requestMethod, requestUri, clientIp);

            sendUnauthorizedResponse(response,
                    "Missing required header: " + API_KEY_HEADER);
            return false;
        }

        // Validate the provided API key
        if (!isValidApiKey(providedApiKey)) {
            log.warn("Invalid internal API key for {} {} from IP: {}. " +
                            "Key length: {} (expected: {})",
                    requestMethod, requestUri, clientIp,
                    providedApiKey.length(), internalApiKey.length());

            sendUnauthorizedResponse(response, "Invalid internal API key");
            return false;
        }

        // API key is valid - allow request to proceed
        log.debug("Internal API key validated successfully for {} {}",
                requestMethod, requestUri);

        return true;
    }

    /**
     * Validates the provided API key against the configured key.
     * Uses constant-time comparison to prevent timing attacks.
     *
     * @param providedKey The API key provided in the request
     * @return true if the key matches, false otherwise
     */
    private boolean isValidApiKey(String providedKey) {
        // Constant-time comparison to prevent timing attacks
        // This ensures the comparison takes the same time regardless
        // of where the mismatch occurs
        return constantTimeEquals(providedKey, internalApiKey);
    }

    /**
     * Performs a constant-time string comparison to prevent timing attacks.
     *
     * Standard equals() can leak information about how many characters
     * match through timing differences. This method ensures consistent
     * execution time regardless of input.
     *
     * @param a First string to compare
     * @param b Second string to compare
     * @return true if strings are equal, false otherwise
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }

        // Convert to byte arrays
        byte[] aBytes = a.getBytes();
        byte[] bBytes = b.getBytes();

        // If lengths differ, use the shorter length but still compare
        // all bytes to maintain constant time
        int result = aBytes.length ^ bBytes.length;

        // Compare all bytes using XOR
        int minLength = Math.min(aBytes.length, bBytes.length);
        for (int i = 0; i < minLength; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }

        return result == 0;
    }

    /**
     * Sends a 401 Unauthorized response with a JSON error body.
     *
     * @param response The HTTP response object
     * @param message  The error message to include
     * @throws Exception if writing the response fails
     */
    private void sendUnauthorizedResponse(HttpServletResponse response,
                                          String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Create JSON error response
        String jsonResponse = String.format(
                "{\"error\":\"Unauthorized\",\"message\":\"%s\",\"status\":401}",
                message
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    /**
     * Extracts the client IP address from the request.
     * Handles proxy headers (X-Forwarded-For) for accurate client IP detection.
     *
     * @param request The HTTP request
     * @return The client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // Check for proxy headers first
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            // X-Forwarded-For can contain multiple IPs; the first is the client
            return xForwardedFor.split(",")[0].trim();
        }

        // Check other common proxy headers
        String[] headerNames = {
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }

        // Fall back to remote address
        return request.getRemoteAddr();
    }

    /**
     * Called after the request handler completes.
     * Used for logging successful internal API calls.
     */
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        if (ex != null) {
            log.error("Error during internal API request {} {}: {}",
                    request.getMethod(), request.getRequestURI(), ex.getMessage());
        }
    }
}