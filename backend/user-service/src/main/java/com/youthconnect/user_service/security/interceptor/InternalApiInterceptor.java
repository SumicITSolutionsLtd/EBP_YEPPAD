package com.youthconnect.user_service.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Minimal internal API interceptor for user-service
 * Only validates internal API keys, doesn't handle JWT
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class InternalApiInterceptor implements HandlerInterceptor {

    @Value("${app.security.internal-api-key}")
    private String internalApiKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String apiKey = request.getHeader("X-Internal-API-Key");

        if (apiKey == null || !apiKey.equals(internalApiKey)) {
            log.warn("Invalid internal API key for request: {}", request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid internal API key");
            return false;
        }

        return true;
    }
}