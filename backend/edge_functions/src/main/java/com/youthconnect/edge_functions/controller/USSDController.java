package com.youthconnect.edge_functions.controller;

import com.youthconnect.edge_functions.service.USSDService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * USSD Controller for Africa's Talking Integration
 *
 * Handles USSD requests from feature phone users (*256#)
 * Provides menu-driven interface for platform access without internet
 *
 * @author Douglas Kings Kato
 * @version 1.0
 */
@RestController
@RequestMapping("/api/ussd")
@RequiredArgsConstructor
@Slf4j
public class USSDController {

    private final USSDService ussdService;

    /**
     * Main USSD callback endpoint for Africa's Talking
     *
     * Request format from Africa's Talking:
     * - sessionId: Unique session identifier
     * - serviceCode: USSD code dialed (e.g., *256#)
     * - phoneNumber: User's phone number
     * - text: User's menu navigation path (e.g., "1*2*3")
     *
     * Response format:
     * - "CON <menu_text>" - Continue session with menu
     * - "END <final_message>" - End session with message
     *
     * @param requestBody USSD request parameters
     * @return USSD response (CON or END)
     */
    @PostMapping(
            value = "/callback",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> handleUSSDCallback(
            @RequestParam Map<String, String> requestBody
    ) {
        try {
            // Extract parameters
            String sessionId = requestBody.get("sessionId");
            String phoneNumber = requestBody.get("phoneNumber");
            String text = requestBody.getOrDefault("text", "");

            log.info("USSD Request - Session: {}, Phone: {}, Text: '{}'",
                    sessionId, phoneNumber, text);

            // Validate required parameters
            if (sessionId == null || sessionId.isEmpty()) {
                log.error("Missing sessionId in USSD request");
                return ResponseEntity.ok("END Error: Invalid session. Please try again.");
            }

            if (phoneNumber == null || phoneNumber.isEmpty()) {
                log.error("Missing phoneNumber in USSD request");
                return ResponseEntity.ok("END Error: Phone number required. Please try again.");
            }

            // Process USSD request
            String response = ussdService.handleUSSDRequest(sessionId, phoneNumber, text);

            log.info("USSD Response - Session: {}, Response: '{}'", sessionId, response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("USSD processing error: {}", e.getMessage(), e);
            return ResponseEntity.ok(
                    "END Service temporarily unavailable. Please try again later."
            );
        }
    }

    /**
     * Test endpoint for USSD simulation (development only)
     *
     * Allows testing USSD flows without actual telecom integration
     *
     * @param sessionId Session identifier
     * @param phoneNumber User phone number
     * @param text User input path
     * @return USSD response
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testUSSD(
            @RequestParam String sessionId,
            @RequestParam String phoneNumber,
            @RequestParam(defaultValue = "") String text
    ) {
        try {
            log.info("USSD Test - Session: {}, Phone: {}, Text: '{}'",
                    sessionId, phoneNumber, text);

            String response = ussdService.handleUSSDRequest(sessionId, phoneNumber, text);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "sessionId", sessionId,
                    "phoneNumber", phoneNumber,
                    "userInput", text,
                    "response", response,
                    "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("USSD test error: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "response", "END Service error. Please try again."
            ));
        }
    }

    /**
     * Get USSD session information (for debugging)
     *
     * @param sessionId Session identifier
     * @return Session details
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSessionInfo(
            @PathVariable String sessionId
    ) {
        try {
            // This would typically retrieve session data from Redis/database
            // For now, return basic info
            log.info("Fetching session info for: {}", sessionId);

            return ResponseEntity.ok(Map.of(
                    "sessionId", sessionId,
                    "message", "Session query not yet implemented",
                    "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("Error fetching session info: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Health check endpoint
     *
     * @return Service health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "USSD Service",
                "timestamp", System.currentTimeMillis()
        ));
    }
}