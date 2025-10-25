package com.youthconnect.ussd_service.controller;

import com.youthconnect.ussd_service.config.MonitoringConfig;
import com.youthconnect.ussd_service.exception.UssdSecurityException;
import com.youthconnect.ussd_service.service.UssdMenuService;
import com.youthconnect.ussd_service.util.RequestValidator;
import com.youthconnect.ussd_service.util.SecurityUtil;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Secure USSD Controller with comprehensive security measures.
 *
 * @author Douglas Kings Kato & Harold
 * @version 2.0.0
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/ussd", produces = MediaType.TEXT_PLAIN_VALUE)
@RequiredArgsConstructor
@Validated
public class UssdController {

    private final UssdMenuService ussdMenuService;
    private final SecurityUtil securityUtil;
    private final RequestValidator requestValidator;
    private final MonitoringConfig monitoringConfig; // ✓ FIXED: Use MonitoringConfig instead of individual counters

    /**
     * Africa's Talking USSD callback endpoint.
     */
    @PostMapping(path = "/at-callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> atUssdCallback(
            HttpServletRequest request,
            @RequestParam("sessionId")
            @NotBlank(message = "Session ID is required")
            @Size(min = 8, max = 64, message = "Session ID must be between 8 and 64 characters")
            String sessionId,

            @RequestParam("serviceCode")
            @NotBlank(message = "Service code is required")
            @Pattern(regexp = "^\\*\\d{1,6}(\\*\\d{1,6})*#?$", message = "Invalid service code format")
            String serviceCode,

            @RequestParam("phoneNumber")
            @NotBlank(message = "Phone number is required")
            @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
            String phoneNumber,

            @RequestParam(value = "text", required = false)
            @Size(max = 500, message = "Text input too long")
            String text,

            @RequestParam(value = "networkCode", required = false) String networkCode,
            @RequestParam(value = "operator", required = false) String operator
    ) {
        Timer.Sample sample = Timer.start();

        try {
            log.info("Received Africa's Talking USSD callback - IP: {}, Session: {}, Phone: {}, Text: '{}', Operator: {}",
                    securityUtil.getClientIPAddress(request), sessionId, phoneNumber, text, operator);

            // 1. IP Whitelisting
            if (!securityUtil.isAllowedIP(request)) {
                String clientIP = securityUtil.getClientIPAddress(request);
                log.warn("SECURITY VIOLATION: USSD request from unauthorized IP: {}", clientIP);
                securityUtil.logSecurityEvent("UNAUTHORIZED_IP", request,
                        "USSD callback from unauthorized IP: " + clientIP);

                // ✓ FIXED: Use MonitoringConfig helper method
                monitoringConfig.recordSecurityEvent("unauthorized_ip", "high", clientIP);

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("END Access denied");
            }

            // 2. Request Validation
            try {
                requestValidator.validateAtCallbackParams(sessionId, serviceCode, phoneNumber, text, request);
                log.debug("Request validation passed for session: {}", sessionId);
            } catch (UssdSecurityException e) {
                log.warn("Request validation failed: {}", e.getMessage());

                // ✓ FIXED: Use helper method
                monitoringConfig.recordSecurityEvent("validation_failure", "medium",
                        securityUtil.getClientIPAddress(request));

                return ResponseEntity.badRequest()
                        .body("END Invalid request format");
            }

            // 3. Input Sanitization
            sessionId = securityUtil.sanitizeUserInput(sessionId);
            phoneNumber = securityUtil.sanitizeUserInput(phoneNumber);
            serviceCode = securityUtil.sanitizeUserInput(serviceCode);
            if (text != null) {
                text = securityUtil.sanitizeUserInput(text);
            }

            // 4. Convert to internal DTO
            UssdRequestDTO internalRequest = new UssdRequestDTO(
                    sessionId, serviceCode, phoneNumber, text, networkCode
            );

            // 5. Process request
            String response = processUssdRequest(internalRequest);

            // 6. Success metrics - ✓ FIXED
            monitoringConfig.recordUssdRequest("success", "africas_talking",
                    operator != null ? operator : "unknown");

            log.info("Successfully processed AT USSD request - Session: {}, Response length: {}",
                    sessionId, response.length());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing Africa's Talking USSD request: {}", e.getMessage(), e);

            // ✓ FIXED: Error metrics
            monitoringConfig.recordUssdRequest("error", "africas_talking",
                    operator != null ? operator : "unknown");

            return ResponseEntity.ok("END System error. Please try again later.");
        }
    }

    /**
     * Test endpoint for USSD functionality.
     */
    @PostMapping(path = "/test", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> testUssd(
            HttpServletRequest request,
            @RequestBody @Valid UssdRequestDTO requestDto
    ) {
        Timer.Sample sample = Timer.start();

        try {
            log.info("Received test USSD request - IP: {}, Phone: {}, Session: {}, Text: '{}'",
                    securityUtil.getClientIPAddress(request), requestDto.getPhoneNumber(),
                    requestDto.getSessionId(), requestDto.getText());

            if (requestDto.getPhoneNumber() == null || requestDto.getSessionId() == null) {
                log.error("Missing required parameters in test request");
                return ResponseEntity.badRequest().body("END Invalid request parameters");
            }

            String response = processUssdRequest(requestDto);

            // ✓ FIXED: Test metrics
            monitoringConfig.recordUssdRequest("success", "test", "test");

            log.info("Test USSD request processed successfully - Session: {}", requestDto.getSessionId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing test USSD request: {}", e.getMessage(), e);

            // ✓ FIXED: Error metrics
            monitoringConfig.recordUssdRequest("error", "test", "test");

            return ResponseEntity.ok("END An error occurred during testing.");
        }
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck(HttpServletRequest request) {
        String clientIP = securityUtil.getClientIPAddress(request);
        boolean isAllowedIP = securityUtil.isAllowedIP(request);

        log.info("Health check requested from IP: {} (allowed: {})", clientIP, isAllowedIP);

        return ResponseEntity.ok(
                "USSD Service is running\n" +
                        "Client IP: " + clientIP + "\n" +
                        "IP Allowed: " + isAllowedIP + "\n" +
                        "Security: Enabled\n" +
                        "Status: Healthy"
        );
    }

    /**
     * Central method for processing USSD requests.
     */
    private String processUssdRequest(UssdRequestDTO request) {
        String cleanPhoneNumber = request.getPhoneNumber();
        if (cleanPhoneNumber.startsWith("+")) {
            cleanPhoneNumber = cleanPhoneNumber.substring(1);
        }

        String text = request.getText() != null ? request.getText() : "";

        String response = ussdMenuService.handleUssdRequest(text, cleanPhoneNumber, request.getSessionId());

        log.debug("Generated USSD response for session {}: {}", request.getSessionId(), response);
        return response;
    }

    /**
     * DTO class for USSD request payload.
     */
    public static class UssdRequestDTO {
        @NotBlank(message = "Session ID is required")
        @Size(min = 8, max = 64)
        private String sessionId;

        @Pattern(regexp = "^\\*\\d{1,6}(\\*\\d{1,6})*#?$")
        private String serviceCode;

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
        private String phoneNumber;

        @Size(max = 500)
        private String text;

        @Size(max = 20)
        private String networkCode;

        public UssdRequestDTO() {}

        public UssdRequestDTO(String sessionId, String serviceCode, String phoneNumber, String text, String networkCode) {
            this.sessionId = sessionId;
            this.serviceCode = serviceCode;
            this.phoneNumber = phoneNumber;
            this.text = text;
            this.networkCode = networkCode;
        }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getServiceCode() { return serviceCode; }
        public void setServiceCode(String serviceCode) { this.serviceCode = serviceCode; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getNetworkCode() { return networkCode; }
        public void setNetworkCode(String networkCode) { this.networkCode = networkCode; }

        @Override
        public String toString() {
            return "UssdRequestDTO{sessionId='" + sessionId + "', phoneNumber='" + phoneNumber + "'}";
        }
    }
}