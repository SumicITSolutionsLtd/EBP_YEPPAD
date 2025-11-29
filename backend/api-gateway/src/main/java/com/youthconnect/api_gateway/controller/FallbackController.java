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
 * ============================================================================
 *  Youth Connect Uganda - API Gateway Fallback Controller
 * ============================================================================
 *
 *  Provides fallback endpoints when backend microservices become unavailable
 *  due to circuit breaker state transitions.
 *
 *  CIRCUIT BREAKER STATES:
 *  - CLOSED: Normal operation, all requests flow through to backend
 *  - OPEN: Service failing, requests immediately return fallback response
 *  - HALF_OPEN: Testing recovery, limited requests allowed through
 *
 *  PURPOSE:
 *  - Graceful degradation when services fail
 *  - User-friendly error messages
 *  - Prevents cascading failures across microservices
 *  - Maintains system stability during partial outages
 *
 *  PRIORITY LEVELS:
 *  - P0 (CRITICAL): Fix immediately - blocks core functionality
 *  - P1 (HIGH): Fix within 1-2 hours - major feature unavailable
 *  - P2 (MEDIUM): Fix within 4 hours - important but not blocking
 *  - P3 (LOW): Fix within 8 hours - minimal user impact
 *
 *  @author Douglas Kings Kato
 *  @version 2.0.0
 */
@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    // =========================================================================
    // AUTHENTICATION SERVICE FALLBACK
    // =========================================================================
    /**
     * Fallback for Authentication Service
     *
     * Triggered when: auth-service is down, circuit breaker opens, or timeout occurs
     *
     * BUSINESS IMPACT: CRITICAL
     * - Users cannot login
     * - Users cannot register
     * - Token validation fails
     * - All authenticated endpoints become inaccessible
     *
     * PRIORITY: P0 - Fix immediately
     *
     * RECOMMENDATIONS:
     * - Check auth-service health endpoint
     * - Verify database connectivity
     * - Check JWT secret configuration
     * - Review recent deployments
     */
    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authServiceFallback() {
        log.error("⚠️ AUTH SERVICE FALLBACK TRIGGERED - Service unavailable");
        log.error("Impact: Users cannot authenticate - CRITICAL ISSUE");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Authentication service is temporarily unavailable. Please try again in a few moments.");
        response.put("service", "auth-service");
        response.put("impact", "CRITICAL");
        response.put("helpText", "If this persists, contact support@youthconnect.ug");
        response.put("suggestedAction", "Wait 1-2 minutes and try again");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    // =========================================================================
    // USER SERVICE FALLBACK
    // =========================================================================
    /**
     * Fallback for User Service
     *
     * Triggered when: user-service is down or circuit breaker opens
     *
     * BUSINESS IMPACT: HIGH
     * - User profiles cannot be viewed or updated
     * - Profile pictures unavailable
     * - User search not working
     * - Registration may be affected
     *
     * PRIORITY: P1 - Fix within 1 hour
     *
     * AFFECTED FEATURES:
     * - /api/users/** endpoints
     * - Profile management
     * - User listings
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        log.warn("⚠️ USER SERVICE FALLBACK TRIGGERED - Service unavailable");
        log.warn("Impact: User profiles and data temporarily inaccessible");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "User service is temporarily unavailable. Please try again in a few moments.");
        response.put("service", "user-service");
        response.put("impact", "HIGH");
        response.put("helpText", "Your profile data is safe. Please retry in a moment.");
        response.put("dataIntegrity", "All user data is safe and will be available when service recovers");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    // =========================================================================
    // FILE MANAGEMENT SERVICE FALLBACK
    // =========================================================================
    /**
     * Fallback for File Management Service
     *
     * Triggered when: file-management-service is down or circuit breaker opens
     *
     * BUSINESS IMPACT: MEDIUM-HIGH
     * - Profile picture uploads fail
     * - Document uploads for job applications fail
     * - Learning module materials unavailable
     * - Resume/CV uploads blocked
     * - Opportunity documents inaccessible
     *
     * PRIORITY: P1 - Fix within 1-2 hours
     *
     * SPECIAL HANDLING:
     * - Failed uploads should be queued for retry
     * - Downloads can be retried automatically
     * - Inform users files will be processed when service recovers
     * - No data loss - files are stored persistently
     *
     * AFFECTED FEATURES:
     * - /api/files/upload
     * - /api/files/download/**
     * - Profile picture updates
     * - Document submissions
     */
    @GetMapping("/files")
    public ResponseEntity<Map<String, Object>> fileServiceFallback() {
        log.warn("⚠️ FILE SERVICE FALLBACK TRIGGERED - Service unavailable");
        log.warn("Impact: File uploads/downloads temporarily blocked");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "File management service is temporarily unavailable. Your files are safe.");
        response.put("service", "file-management-service");
        response.put("impact", "MEDIUM_HIGH");
        response.put("helpText", "If you were uploading a file, please retry in a moment. Your data has not been lost.");

        // Detailed recommendations for different file operations
        response.put("recommendations", Map.of(
                "forUploads", "Save your file locally and retry upload in 1-2 minutes",
                "forDownloads", "Click download again - your files are still safely stored",
                "forLearning", "Learning modules will be accessible again shortly",
                "forApplications", "Application documents can be uploaded once service recovers"
        ));

        response.put("dataIntegrity", "All previously uploaded files are safe and secure");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    // =========================================================================
    // JOB SERVICE FALLBACK
    // =========================================================================
    /**
     * Fallback for Job Service
     *
     * Triggered when: job-services is down or circuit breaker opens
     *
     * BUSINESS IMPACT: MEDIUM
     * - Job listings unavailable
     * - Cannot post new jobs
     * - Cannot apply to jobs
     * - Job search not working
     *
     * PRIORITY: P2 - Fix within 4 hours
     *
     * NOTE: Existing applications are safe in database
     */
    @GetMapping("/jobs")
    public ResponseEntity<Map<String, Object>> jobServiceFallback() {
        log.warn("⚠️ JOB SERVICE FALLBACK TRIGGERED - Service unavailable");
        log.warn("Impact: Job listings and applications temporarily unavailable");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Job service is temporarily unavailable. Please try again in a few moments.");
        response.put("service", "job-services");
        response.put("impact", "MEDIUM");
        response.put("helpText", "You can still browse other features while we restore job listings.");
        response.put("dataIntegrity", "All job applications and postings are saved and will be accessible when service recovers");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    // =========================================================================
    // OPPORTUNITY SERVICE FALLBACK
    // =========================================================================
    /**
     * Fallback for Opportunity Service
     *
     * Triggered when: opportunity-service is down or circuit breaker opens
     *
     * BUSINESS IMPACT: HIGH
     * - Funding opportunities inaccessible
     * - Grant listings unavailable
     * - Loan opportunities hidden
     * - Cannot apply for opportunities
     *
     * PRIORITY: P1 - Fix within 2 hours
     *
     * CRITICAL: Deadlines may be time-sensitive for opportunities
     */
    @GetMapping("/opportunities")
    public ResponseEntity<Map<String, Object>> opportunityServiceFallback() {
        log.warn("⚠️ OPPORTUNITY SERVICE FALLBACK TRIGGERED - Service unavailable");
        log.warn("Impact: Grant and loan opportunities temporarily inaccessible");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Opportunity service is temporarily unavailable. Please try again in a few moments.");
        response.put("service", "opportunity-service");
        response.put("impact", "HIGH");
        response.put("helpText", "Grant and loan opportunities will be back shortly.");
        response.put("urgentNote", "If you have a deadline approaching, contact support@youthconnect.ug");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    // =========================================================================
    // MENTORSHIP SERVICE FALLBACK
    // =========================================================================
    /**
     * Fallback for Mentorship Service
     *
     * Triggered when: mentor-service is down or circuit breaker opens
     *
     * BUSINESS IMPACT: MEDIUM
     * - Cannot book new mentorship sessions
     * - Mentor search unavailable
     * - Session history may be inaccessible
     *
     * PRIORITY: P2 - Fix within 4 hours
     *
     * NOTE: Existing scheduled sessions are NOT affected
     */
    @GetMapping("/mentorship")
    public ResponseEntity<Map<String, Object>> mentorshipServiceFallback() {
        log.warn("⚠️ MENTORSHIP SERVICE FALLBACK TRIGGERED - Service unavailable");
        log.warn("Impact: New mentorship bookings temporarily unavailable");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Mentorship service is temporarily unavailable. Please try again in a few moments.");
        response.put("service", "mentor-service");
        response.put("impact", "MEDIUM");
        response.put("helpText", "Scheduled sessions are not affected. Only new bookings are temporarily unavailable.");
        response.put("reassurance", "Your existing mentorship sessions will proceed as scheduled");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    // =========================================================================
    // NOTIFICATION SERVICE FALLBACK
    // =========================================================================
    /**
     * Fallback for Notification Service
     *
     * Triggered when: notification-service is down or circuit breaker opens
     *
     * BUSINESS IMPACT: LOW
     * - Email notifications delayed
     * - SMS notifications queued
     * - In-app notifications not sent
     *
     * PRIORITY: P3 - Fix within 8 hours
     *
     * NOTE: Notifications are queued and will be sent when service recovers
     */
    @GetMapping("/notifications")
    public ResponseEntity<Map<String, Object>> notificationServiceFallback() {
        log.info("ℹ️ NOTIFICATION SERVICE FALLBACK TRIGGERED - Service unavailable");
        log.info("Impact: Notifications queued for later delivery - Low priority issue");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "Notification service is temporarily unavailable. Notifications will be queued and sent when the service recovers.");
        response.put("service", "notification-service");
        response.put("impact", "LOW");
        response.put("helpText", "Your notifications will be delivered once the service is back online.");
        response.put("queueStatus", "All notifications are safely queued and will not be lost");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    // =========================================================================
    // GENERIC FALLBACK
    // =========================================================================
    /**
     * Generic fallback for services without specific handlers
     *
     * Used as catch-all when no specific fallback is configured
     *
     * BUSINESS IMPACT: UNKNOWN
     * PRIORITY: Depends on service
     *
     * RECOMMENDATION: Add specific fallback handler for frequently used services
     */
    @GetMapping("/generic")
    public ResponseEntity<Map<String, Object>> genericFallback() {
        log.warn("⚠️ GENERIC FALLBACK TRIGGERED - Unconfigured service failure");
        log.warn("Consider adding specific fallback for this service");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", "The requested service is temporarily unavailable. Please try again later.");
        response.put("impact", "UNKNOWN");
        response.put("helpText", "If this issue persists, please contact support@youthconnect.ug");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    // =========================================================================
    // UTILITY METHODS (Optional - for future enhancements)
    // =========================================================================

    /**
     * Helper method to create standardized fallback response
     * Can be used to reduce code duplication in future updates
     */
    private Map<String, Object> createBaseFallbackResponse(
            String serviceName,
            String impact,
            String message) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("service", serviceName);
        response.put("impact", impact);
        response.put("message", message);

        return response;
    }
}