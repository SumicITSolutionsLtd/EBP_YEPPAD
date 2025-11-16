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
 * COMPLETE VERSION - Including File Service
 *
 * Provides fallback endpoints when backend services are unavailable
 * due to circuit breaker opening (too many failures).
 *
 * These endpoints return user-friendly error messages instead of
 * letting requests fail completely.
 *
 * CIRCUIT BREAKER STATES:
 * - CLOSED: Normal operation, requests flow through
 * - OPEN: Too many failures, requests fail immediately with fallback
 * - HALF_OPEN: Testing if service recovered, limited requests allowed
 *
 * Location: api-gateway/src/main/java/com/youthconnect/api_gateway/controller/
 *
 * @author Douglas Kings Kato
 * @version 2.0.0 (Complete with File Service)
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    /**
     * Fallback for Auth Service
     * Triggered when auth-service is down or circuit breaker is open
     *
     * BUSINESS IMPACT: Critical - Users cannot login/register
     * PRIORITY: P0 - Fix immediately
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
        response.put("impact", "CRITICAL");
        response.put("helpText", "If this persists, contact support@youthconnect.ug");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    /**
     * Fallback for User Service
     *
     * BUSINESS IMPACT: High - User profiles inaccessible
     * PRIORITY: P1 - Fix within 1 hour
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
        response.put("impact", "HIGH");
        response.put("helpText", "Your profile data is safe. Please retry in a moment.");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    /**
     * ⭐ NEW: Fallback for File Management Service
     * Triggered when file-management-service is down or circuit breaker is open
     *
     * BUSINESS IMPACT: Medium-High
     * - Users cannot upload profile pictures
     * - Cannot upload documents for applications
     * - Learning modules unavailable
     *
     * PRIORITY: P1 - Fix within 1-2 hours
     *
     * SPECIAL HANDLING:
     * - File uploads that fail should be queued for retry
     * - Downloads can be retried automatically by client
     * - Inform users their uploads will be processed when service recovers
     */
    @GetMapping("/files")
    public ResponseEntity<Map<String, Object>> fileServiceFallback() {
        log.warn("File service fallback triggered - service may be down");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "File management service is temporarily unavailable. Your files are safe.");
        response.put("service", "file-management-service");
        response.put("impact", "MEDIUM_HIGH");
        response.put("helpText", "If you were uploading a file, please retry in a moment. Your data has not been lost.");
        response.put("recommendations", Map.of(
                "forUploads", "Save your file and retry upload in 1-2 minutes",
                "forDownloads", "Click download again - your files are still available",
                "forLearning", "Learning modules will be back shortly"
        ));

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    /**
     * Fallback for Job Service
     *
     * BUSINESS IMPACT: Medium - Job listings unavailable
     * PRIORITY: P2 - Fix within 4 hours
     */
    @GetMapping("/jobs")
    public ResponseEntity<Map<String, Object>> jobServiceFallback() {
        log.warn("Job service fallback triggered - service may be down");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Job service is temporarily unavailable. Please try again in a few moments.");
        response.put("service", "job-services");
        response.put("impact", "MEDIUM");
        response.put("helpText", "You can still browse other features while we restore job listings.");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    /**
     * Fallback for Opportunity Service
     *
     * BUSINESS IMPACT: High - Funding opportunities inaccessible
     * PRIORITY: P1 - Fix within 2 hours
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
        response.put("impact", "HIGH");
        response.put("helpText", "Grant and loan opportunities will be back shortly.");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    /**
     * Fallback for Mentorship Service
     *
     * BUSINESS IMPACT: Medium - Mentorship bookings unavailable
     * PRIORITY: P2 - Fix within 4 hours
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
        response.put("impact", "MEDIUM");
        response.put("helpText", "Scheduled sessions are not affected. Only new bookings are temporarily unavailable.");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    /**
     * Generic fallback for other services
     *
     * Used when no specific fallback is defined
     */
    @GetMapping("/generic")
    public ResponseEntity<Map<String, Object>> genericFallback() {
        log.warn("Generic fallback triggered");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "The requested service is temporarily unavailable. Please try again later.");
        response.put("impact", "UNKNOWN");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    /**
     * Fallback for Notification Service
     *
     * BUSINESS IMPACT: Low - Notifications will be queued
     * PRIORITY: P3 - Fix within 8 hours
     */
    @GetMapping("/notifications")
    public ResponseEntity<Map<String, Object>> notificationServiceFallback() {
        log.warn("Notification service fallback triggered - service may be down");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Notification service is temporarily unavailable. Notifications will be queued and sent when the service recovers.");
        response.put("service", "notification-service");
        response.put("impact", "LOW");
        response.put("helpText", "Your notifications will be delivered once the service is back online.");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}