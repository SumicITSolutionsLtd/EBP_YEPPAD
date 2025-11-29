package com.youthconnect.user_service.config;

import com.youthconnect.user_service.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * GLOBAL RESPONSE HANDLER
 * ═══════════════════════════════════════════════════════════════════════════
 * Standardizes API responses.
 * ✅ Skips Swagger (fixes Base64 bug)
 * ✅ Skips Actuator (fixes monitoring tools)
 */
@Slf4j
@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String requestUri = attributes.getRequest().getRequestURI();
                // ✅ Check exclusion BEFORE attempting to wrap
                if (isExcludedPath(requestUri)) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.trace("Could not access request context: {}", e.getMessage());
        }

        // Skip if already ApiResponse
        return !ApiResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // Double check URL as safety net
        String path = request.getURI().getPath();
        if (isExcludedPath(path)) {
            return body;
        }

        if (body instanceof ApiResponse) {
            return body;
        }

        return ApiResponse.success(body, "Operation completed successfully");
    }

    private boolean isExcludedPath(String path) {
        if (path == null) return false;

        // ✅ EXCLUDE SWAGGER (Fixes "Unable to render definition")
        if (path.contains("/v3/api-docs") || path.contains("/swagger-ui") || path.contains("/webjars")) {
            return true;
        }

        // ✅ EXCLUDE ACTUATOR (Standard monitoring)
        if (path.contains("/actuator")) {
            return true;
        }

        return false;
    }
}