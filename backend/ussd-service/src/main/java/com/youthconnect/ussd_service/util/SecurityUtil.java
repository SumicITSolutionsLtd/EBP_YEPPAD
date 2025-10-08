package com.youthconnect.ussd_service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for USSD security operations including IP validation,
 * request origin verification, and security-related helper methods.
 *
 * This class provides centralized security functions to protect USSD endpoints
 * from unauthorized access and malicious requests.
 */
@Slf4j
@Component
public class SecurityUtil {

    // Africa's Talking IP ranges - These should be configured in application.properties
    // and obtained from Africa's Talking documentation
    @Value("${ussd.security.allowed-ips:196.216.167.0,196.216.168.0,41.210.142.0}")
    private String allowedIpsConfig;

    // Rate limiting configurations
    @Value("${ussd.security.max-requests-per-minute:60}")
    private int maxRequestsPerMinute;

    @Value("${ussd.security.enable-ip-whitelist:true}")
    private boolean enableIpWhitelist;

    // Compiled patterns for better performance
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    private static final Pattern SESSION_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{8,64}$");
    private static final Pattern SERVICE_CODE_PATTERN = Pattern.compile("^\\*\\d{1,6}(\\*\\d{1,6})*#?$");

    /**
     * Validates if the request comes from an allowed IP address.
     * This is crucial for preventing unauthorized USSD callback attempts.
     *
     * @param request The HTTP request to validate
     * @return true if IP is allowed, false otherwise
     */
    public boolean isAllowedIP(HttpServletRequest request) {
        if (!enableIpWhitelist) {
            log.debug("IP whitelist is disabled, allowing all IPs");
            return true;
        }

        String clientIP = getClientIPAddress(request);
        List<String> allowedIPs = getAllowedIPList();

        boolean isAllowed = allowedIPs.contains(clientIP) || isInIPRange(clientIP, allowedIPs);

        if (isAllowed) {
            log.debug("Request from allowed IP: {}", clientIP);
        } else {
            log.warn("Request from unauthorized IP: {} - Allowed IPs: {}", clientIP, allowedIPs);
        }

        return isAllowed;
    }

    /**
     * Extracts the real client IP address from the request, handling proxies and load balancers.
     * Checks common headers used by proxy servers to identify the original client IP.
     *
     * @param request The HTTP request
     * @return The client's IP address
     */
    public String getClientIPAddress(HttpServletRequest request) {
        // Check X-Forwarded-For header (most common for proxies)
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedForHeader)) {
            // X-Forwarded-For can contain multiple IPs, take the first one (original client)
            String clientIP = xForwardedForHeader.split(",")[0].trim();
            if (isValidIP(clientIP)) {
                return clientIP;
            }
        }

        // Check X-Real-IP header (used by some proxies)
        String xRealIP = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIP) && isValidIP(xRealIP)) {
            return xRealIP;
        }

        // Check X-Original-Forwarded-For header
        String xOriginalForwardedFor = request.getHeader("X-Original-Forwarded-For");
        if (StringUtils.hasText(xOriginalForwardedFor)) {
            String clientIP = xOriginalForwardedFor.split(",")[0].trim();
            if (isValidIP(clientIP)) {
                return clientIP;
            }
        }

        // Fallback to remote address
        return request.getRemoteAddr();
    }

    /**
     * Validates phone number format according to international standards.
     * Supports E.164 format and common variations.
     *
     * @param phoneNumber The phone number to validate
     * @return true if valid format, false otherwise
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return false;
        }

        String cleanNumber = phoneNumber.trim();

        // Check basic pattern
        if (!PHONE_NUMBER_PATTERN.matcher(cleanNumber).matches()) {
            return false;
        }

        // Additional validation for Uganda numbers specifically
        if (cleanNumber.startsWith("+256") || cleanNumber.startsWith("256")) {
            String numberPart = cleanNumber.replaceFirst("^\\+?256", "");
            // Uganda numbers should be 9 digits after country code
            return numberPart.matches("^[7-9]\\d{8}$");
        }

        // Generic international number validation
        String digitOnly = cleanNumber.replaceAll("[^0-9]", "");
        return digitOnly.length() >= 10 && digitOnly.length() <= 15;
    }

    /**
     * Validates USSD session ID format to prevent injection attacks.
     *
     * @param sessionId The session ID to validate
     * @return true if valid format, false otherwise
     */
    public boolean isValidSessionId(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return false;
        }

        return SESSION_ID_PATTERN.matcher(sessionId.trim()).matches();
    }

    /**
     * Validates USSD service code format.
     *
     * @param serviceCode The service code to validate
     * @return true if valid format, false otherwise
     */
    public boolean isValidServiceCode(String serviceCode) {
        if (!StringUtils.hasText(serviceCode)) {
            return false;
        }

        return SERVICE_CODE_PATTERN.matcher(serviceCode.trim()).matches();
    }

    /**
     * Sanitizes user input to prevent injection attacks and ensure data integrity.
     * Removes potentially harmful characters while preserving legitimate content.
     *
     * @param input The input string to sanitize
     * @return Sanitized string
     */
    public String sanitizeUserInput(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }

        return input.trim()
                // Remove control characters
                .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "")
                // Limit to reasonable character set for names and responses
                .replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "")
                // Collapse multiple whitespaces
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Checks if the request size is within acceptable limits to prevent DoS attacks.
     *
     * @param contentLength The content length of the request
     * @return true if within limits, false otherwise
     */
    public boolean isValidRequestSize(long contentLength) {
        // USSD requests should be small, typically under 1KB
        final long MAX_CONTENT_LENGTH = 1024; // 1KB

        if (contentLength > MAX_CONTENT_LENGTH) {
            log.warn("Request content length {} exceeds maximum allowed {}", contentLength, MAX_CONTENT_LENGTH);
            return false;
        }

        return true;
    }

    /**
     * Validates request headers for potential security threats.
     *
     * @param request The HTTP request to validate
     * @return true if headers are safe, false otherwise
     */
    public boolean hasValidHeaders(HttpServletRequest request) {
        // Check Content-Type header
        String contentType = request.getContentType();
        if (StringUtils.hasText(contentType)) {
            if (!contentType.toLowerCase().contains("application/x-www-form-urlencoded") &&
                    !contentType.toLowerCase().contains("application/json")) {
                log.warn("Suspicious content type: {}", contentType);
                return false;
            }
        }

        // Check User-Agent header (Africa's Talking should have identifiable UA)
        String userAgent = request.getHeader("User-Agent");
        if (!StringUtils.hasText(userAgent)) {
            log.warn("Missing User-Agent header in USSD request");
            // Don't block based on missing UA alone, but log for monitoring
        }

        return true;
    }

    /**
     * Creates a security event log entry for monitoring and audit purposes.
     *
     * @param event The security event type
     * @param request The HTTP request
     * @param details Additional details about the event
     */
    public void logSecurityEvent(String event, HttpServletRequest request, String details) {
        String clientIP = getClientIPAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String timestamp = java.time.Instant.now().toString();

        log.warn("SECURITY_EVENT: {} | IP: {} | UA: {} | Details: {} | Timestamp: {}",
                event, clientIP, userAgent, details, timestamp);

        // Here you could integrate with external security monitoring systems
        // such as sending alerts to SIEM systems or security dashboards
    }

    // Private helper methods

    /**
     * Gets the list of allowed IP addresses from configuration.
     *
     * @return List of allowed IP addresses
     */
    private List<String> getAllowedIPList() {
        return Arrays.asList(allowedIpsConfig.split(","));
    }

    /**
     * Checks if an IP is within allowed IP ranges.
     * This is a simple implementation - for production, consider using CIDR notation.
     *
     * @param clientIP The IP to check
     * @param allowedIPs List of allowed IP patterns
     * @return true if IP is in allowed range
     */
    private boolean isInIPRange(String clientIP, List<String> allowedIPs) {
        // Simple subnet matching - for production, implement proper CIDR matching
        for (String allowedIP : allowedIPs) {
            if (allowedIP.contains("/")) {
                // CIDR notation - implement proper subnet matching here
                continue;
            }

            // Wildcard matching (e.g., 192.168.1.*)
            if (allowedIP.contains("*")) {
                String pattern = allowedIP.replace("*", ".*");
                if (clientIP.matches(pattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Basic IP address format validation.
     *
     * @param ip The IP address to validate
     * @return true if valid IP format
     */
    private boolean isValidIP(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }

        // Simple IPv4 validation
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }

        for (String part : parts) {
            try {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }
}