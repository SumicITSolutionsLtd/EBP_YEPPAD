package com.youthconnect.api_gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback Controller for Circuit Breaker
 *
 * Provides fallback endpoints when backend services are unavailable
 * due to circuit breaker opening (too many failures).
 *
 * These endpoints return user-friendly error messages instead of
 * letting requests fail completely.
 *
 * Location: api-gateway/src/main/java/com/youthconnect/api_gateway/controller/
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * Fallback for Auth Service
     * Triggered when auth-service is down or circuit breaker is open
     */
    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authServiceFallback() {
        log.warn("Auth service fallback triggered - service may be down");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Authentication service is temporarily unavailable. Please try again in a few moments.");
        response.put("service", "auth-service");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    /**
     * Fallback for User Service
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        log.warn("User service fallback triggered - service may be down");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "User service is temporarily unavailable. Please try again in a few moments.");
        response.put("service", "user-service");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    /**
     * Fallback for Opportunity Service
     */
    @GetMapping("/opportunities")
    public ResponseEntity<Map<String, Object>> opportunityServiceFallback() {
        log.warn("Opportunity service fallback triggered - service may be down");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Opportunity service is temporarily unavailable. Please try again in a few moments.");
        response.put("service", "opportunity-service");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    /**
     * Fallback for Mentorship Service
     */
    @GetMapping("/mentorship")
    public ResponseEntity<Map<String, Object>> mentorshipServiceFallback() {
        log.warn("Mentorship service fallback triggered - service may be down");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Mentorship service is temporarily unavailable. Please try again in a few moments.");
        response.put("service", "mentor-service");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    /**
     * Generic fallback for other services
     */
    @GetMapping("/generic")
    public ResponseEntity<Map<String, Object>> genericFallback() {
        log.warn("Generic fallback triggered");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "The requested service is temporarily unavailable. Please try again later.");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}