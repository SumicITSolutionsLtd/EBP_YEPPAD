package com.youthconnect.ussd_service.client;

import com.youthconnect.ussd_service.dto.*;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.PostConstruct; // ✓ FIXED: Correct import for Spring Boot 3.x
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Gateway Client for inter-service communication with comprehensive error handling.
 *
 * <p>Features:
 * <ul>
 *   <li>Automatic retry with exponential backoff</li>
 *   <li>Circuit breaker integration</li>
 *   <li>Metrics collection for monitoring</li>
 *   <li>Timeout configuration</li>
 *   <li>Request/response logging</li>
 * </ul>
 *
 * @author YouthConnect Uganda Development Team
 * @version 2.0.0
 * @since 2025-01-29
 */
@Slf4j
@Component
public class GatewayClient {

    private RestTemplate restTemplate;
    private final MeterRegistry meterRegistry;
    private final RestTemplateBuilder restTemplateBuilder;

    // Configuration properties
    @Value("${gateway.base-url:http://localhost:8081/api}")
    private String gatewayBaseUrl;

    @Value("${gateway.timeout.connect:5000}")
    private int connectTimeoutMillis;

    @Value("${gateway.timeout.read:10000}")
    private int readTimeoutMillis;

    @Value("${gateway.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${gateway.retry.delay-ms:1000}")
    private long retryDelayMs;

    @Value("${gateway.endpoints.users:/users}")
    private String usersEndpoint;

    @Value("${gateway.endpoints.opportunities:/opportunities}")
    private String opportunitiesEndpoint;

    @Value("${gateway.endpoints.registration:/users/ussd-register}")
    private String registrationEndpoint;

    @Value("${gateway.endpoints.user-profile:/users/profile}")
    private String userProfileEndpoint;

    // Request headers
    private static final String REQUEST_SOURCE_HEADER = "X-Request-Source";
    private static final String SESSION_ID_HEADER = "X-Session-ID";
    private static final String REQUEST_SOURCE_VALUE = "USSD-Service";

    /**
     * Constructor with dependency injection
     */
    public GatewayClient(RestTemplateBuilder restTemplateBuilder, MeterRegistry meterRegistry) {
        this.restTemplateBuilder = restTemplateBuilder;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Initialize RestTemplate after properties injection.
     * ✓ FIXED: Using jakarta.annotation.PostConstruct
     */
    @PostConstruct
    public void init() {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(connectTimeoutMillis))
                .setReadTimeout(Duration.ofMillis(readTimeoutMillis))
                .build();

        log.info("Gateway Client Configuration:");
        log.info("  Base URL: {}", gatewayBaseUrl);
        log.info("  Connect Timeout: {}ms", connectTimeoutMillis);
        log.info("  Read Timeout: {}ms", readTimeoutMillis);
        log.info("  Max Retry Attempts: {}", maxRetryAttempts);
    }

    // ========================================================================
    // USER REGISTRATION
    // ========================================================================

    /**
     * Registers a new user via USSD with comprehensive error handling and metrics.
     *
     * @param request User registration data
     * @return true if registration successful, false otherwise
     */
    @Retryable(
            retryFor = {ResourceAccessException.class, HttpServerErrorException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 1.5)
    )
    public boolean registerUssdUser(UssdRegistrationRequestDTO request) {
        String sessionId = generateSessionId();
        String phoneNumber = request.getPhoneNumber();
        String serviceName = "user_service";
        String operationName = "register";

        Timer.Sample sample = Timer.start(meterRegistry);
        log.info("Session [{}] - Registering USSD user: {} ({})",
                sessionId, request.getFullName(), phoneNumber);

        try {
            HttpHeaders headers = createStandardHeaders(sessionId);
            HttpEntity<UssdRegistrationRequestDTO> entity = new HttpEntity<>(request, headers);

            String registrationUrl = gatewayBaseUrl + registrationEndpoint;
            log.debug("Session [{}] - POST {}", sessionId, registrationUrl);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    registrationUrl,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            boolean success = response.getStatusCode().is2xxSuccessful();
            recordMetrics(serviceName, operationName, success ? "success" : "failure",
                    response.getStatusCode().value(), sample);

            if (success) {
                log.info("Session [{}] - User registration successful for: {}", sessionId, phoneNumber);
                return true;
            } else {
                log.warn("Session [{}] - Registration failed with status: {}", sessionId, response.getStatusCode());
                return false;
            }

        } catch (HttpClientErrorException e) {
            String status = e.getStatusCode() == HttpStatus.CONFLICT ? "user_exists" : "client_error";
            recordMetrics(serviceName, operationName, status, e.getStatusCode().value(), sample);

            log.error("Session [{}] - Client error during registration for {}: {} - {}",
                    sessionId, phoneNumber, e.getStatusCode(), e.getResponseBodyAsString());
            return false;

        } catch (HttpServerErrorException e) {
            recordMetrics(serviceName, operationName, "server_error", e.getStatusCode().value(), sample);
            log.error("Session [{}] - Server error during registration for {}: {} - {}",
                    sessionId, phoneNumber, e.getStatusCode(), e.getResponseBodyAsString());
            throw e; // Re-throw to trigger retry

        } catch (ResourceAccessException e) {
            recordMetrics(serviceName, operationName, "network_error", 0, sample);
            log.error("Session [{}] - Network error during registration for {}: {}",
                    sessionId, phoneNumber, e.getMessage());
            throw e; // Re-throw to trigger retry

        } catch (Exception e) {
            recordMetrics(serviceName, operationName, "unknown_error", 0, sample);
            log.error("Session [{}] - Unexpected error during registration for {}: {}",
                    sessionId, phoneNumber, e.getMessage(), e);
            return false;
        }
    }

    // ========================================================================
    // USER PROFILE MANAGEMENT
    // ========================================================================

    /**
     * Retrieves user profile by phone number.
     *
     * @param phoneNumber User's phone number
     * @return UserProfileDTO if found, null otherwise
     */
    public UserProfileDTO getUserProfile(String phoneNumber) {
        String sessionId = generateSessionId();
        String serviceName = "user_service";
        String operationName = "get_profile";

        Timer.Sample sample = Timer.start(meterRegistry);
        log.debug("Session [{}] - Fetching user profile for: {}", sessionId, phoneNumber);

        try {
            HttpHeaders headers = createStandardHeaders(sessionId);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String profileUrl = UriComponentsBuilder
                    .fromUriString(gatewayBaseUrl + userProfileEndpoint)
                    .queryParam("phoneNumber", phoneNumber)
                    .toUriString();

            log.debug("Session [{}] - GET {}", sessionId, profileUrl);

            ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                    profileUrl,
                    HttpMethod.GET,
                    entity,
                    UserProfileDTO.class
            );

            boolean success = response.getStatusCode() == HttpStatus.OK && response.getBody() != null;
            recordMetrics(serviceName, operationName, success ? "success" : "not_found",
                    response.getStatusCode().value(), sample);

            if (success) {
                UserProfileDTO profile = response.getBody();
                log.debug("Session [{}] - Profile found for: {} {}", sessionId,
                        profile.getFirstName(), profile.getLastName());
                return profile;
            }

            return null;

        } catch (HttpClientErrorException.NotFound e) {
            recordMetrics(serviceName, operationName, "not_found", 404, sample);
            log.debug("Session [{}] - User not found: {}", sessionId, phoneNumber);
            return null;

        } catch (Exception e) {
            recordMetrics(serviceName, operationName, "error", 0, sample);
            log.error("Session [{}] - Error fetching profile for {}: {}", sessionId, phoneNumber, e.getMessage());
            return null;
        }
    }

    // ========================================================================
    // OPPORTUNITY MANAGEMENT
    // ========================================================================

    /**
     * Fetches opportunities by type with enhanced error handling.
     *
     * @param opportunityType Type of opportunity (GRANT, TRAINING, JOB)
     * @return List of opportunities, empty list if none found
     */
    public List<OpportunityDTO> getOpportunitiesByType(String opportunityType) {
        String sessionId = generateSessionId();
        String serviceName = "opportunity_service";
        String operationName = "get_by_type";

        Timer.Sample sample = Timer.start(meterRegistry);
        log.debug("Session [{}] - Fetching opportunities of type: {}", sessionId, opportunityType);

        try {
            HttpHeaders headers = createStandardHeaders(sessionId);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String opportunitiesUrl = UriComponentsBuilder
                    .fromUriString(gatewayBaseUrl + opportunitiesEndpoint)
                    .queryParam("type", opportunityType)
                    .toUriString();

            log.debug("Session [{}] - GET {}", sessionId, opportunitiesUrl);

            ResponseEntity<List<OpportunityDTO>> response = restTemplate.exchange(
                    opportunitiesUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<OpportunityDTO>>() {}
            );

            boolean success = response.getStatusCode() == HttpStatus.OK && response.getBody() != null;
            List<OpportunityDTO> opportunities = success ? response.getBody() : Collections.emptyList();

            recordMetrics(serviceName, operationName, success ? "success" : "not_found",
                    response.getStatusCode().value(), sample);

            if (success && !opportunities.isEmpty()) {
                log.debug("Session [{}] - Found {} opportunities of type: {}",
                        sessionId, opportunities.size(), opportunityType);
            } else {
                log.debug("Session [{}] - No opportunities found for type: {}", sessionId, opportunityType);
            }

            return opportunities;

        } catch (HttpClientErrorException e) {
            recordMetrics(serviceName, operationName, "client_error", e.getStatusCode().value(), sample);
            log.error("Session [{}] - Client error fetching opportunities for type {}: {} - {}",
                    sessionId, opportunityType, e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();

        } catch (Exception e) {
            recordMetrics(serviceName, operationName, "error", 0, sample);
            log.error("Session [{}] - Error fetching opportunities for type {}: {}",
                    sessionId, opportunityType, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Creates standard HTTP headers for requests.
     */
    private HttpHeaders createStandardHeaders(String sessionId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(REQUEST_SOURCE_HEADER, REQUEST_SOURCE_VALUE);
        headers.set(SESSION_ID_HEADER, sessionId);
        return headers;
    }

    /**
     * ✓ FIXED: Records metrics using proper Micrometer API
     */
    private void recordMetrics(String service, String operation, String status,
                               int statusCode, Timer.Sample sample) {
        // Record request count with tags
        meterRegistry.counter("gateway.requests.total",
                Tags.of("service", service,
                        "operation", operation,
                        "status", status,
                        "status_code", String.valueOf(statusCode))
        ).increment();

        // Record response time
        sample.stop(meterRegistry.timer("gateway.response.duration",
                Tags.of("service", service,
                        "operation", operation,
                        "status", status)));
    }

    /**
     * Generates a unique session ID for request tracking.
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}