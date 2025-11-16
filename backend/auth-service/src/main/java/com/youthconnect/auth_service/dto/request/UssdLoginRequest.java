package com.youthconnect.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * USSD Login Request DTO
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Used for simplified USSD authentication where:
 * - Only phone number is required (no password)
 * - Session ID tracks USSD interaction
 * - Supports feature phone users with limited input capabilities
 *
 * SECURITY NOTE:
 * USSD login is less secure than web login as it lacks password verification.
 * This is acceptable for USSD due to:
 * - Phone number ownership verification via telecom
 * - Limited transaction capabilities in USSD interface
 * - Target audience (rural users with feature phones)
 *
 * VALIDATION:
 * - Phone must match Ugandan format (+256XXXXXXXXX or 0XXXXXXXXX)
 * - Session ID is optional but recommended for state tracking
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UssdLoginRequest {

    /**
     * User's phone number in Ugandan format
     *
     * ACCEPTED FORMATS:
     * - International: +256700000000 (12 digits including country code)
     * - Local: 0700000000 (10 digits starting with 0)
     *
     * VALIDATION PATTERN:
     * - ^\\+?256[0-9]{9}$ matches: +256700000000
     * - ^0[0-9]{9}$ matches: 0700000000
     *
     * EXAMPLES:
     * ✅ Valid: +256700000000, +256750123456, 0700000000, 0750123456
     * ❌ Invalid: 256700000000, +25670000, 700000000, +256-700-000-000
     */
    @NotBlank(message = "Phone number is required for USSD login")
    @Pattern(
            regexp = "^(\\+?256[0-9]{9}|0[0-9]{9})$",
            message = "Invalid Ugandan phone number format. Use +256XXXXXXXXX or 0XXXXXXXXX"
    )
    private String phoneNumber;

    /**
     * USSD session identifier from telecom provider
     *
     * PURPOSE:
     * - Tracks multi-step USSD interactions
     * - Maintains state across USSD menu navigation
     * - Enables session-based rate limiting
     *
     * FORMAT (Africa's Talking):
     * - Prefix: "ATUid_" followed by alphanumeric string
     * - Example: "ATUid_abc123xyz789"
     *
     * OPTIONAL: Can be null for stateless USSD flows
     */
    private String sessionId;

    /**
     * Additional metadata for USSD context (optional)
     * Used for advanced USSD flows with multi-step authentication
     */
    private String menuLevel;  // e.g., "LOGIN", "VERIFY", "COMPLETE"

    /**
     * Validates phone number format
     * Helper method for additional validation beyond annotation
     *
     * @return true if phone number is valid Ugandan format
     */
    public boolean isValidPhoneNumber() {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)\\.]", "");

        // Check international format
        if (cleaned.matches("^\\+256[0-9]{9}$")) {
            return true;
        }

        // Check local format
        if (cleaned.matches("^0[0-9]{9}$")) {
            return true;
        }

        return false;
    }

    /**
     * Normalizes phone number to international format
     * Converts local format (0700000000) to international (+256700000000)
     *
     * @return Normalized phone number in +256XXXXXXXXX format
     */
    public String getNormalizedPhoneNumber() {
        if (phoneNumber == null) {
            return null;
        }

        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)\\.]", "");

        // Already international format
        if (cleaned.startsWith("+256")) {
            return cleaned;
        }

        // Convert local to international
        if (cleaned.startsWith("0") && cleaned.length() == 10) {
            return "+256" + cleaned.substring(1);
        }

        // If starts with 256 but no +
        if (cleaned.startsWith("256") && cleaned.length() == 12) {
            return "+" + cleaned;
        }

        // Return as-is if format is unknown
        return cleaned;
    }

    /**
     * Checks if session ID is present
     *
     * @return true if session ID exists and is not blank
     */
    public boolean hasSessionId() {
        return sessionId != null && !sessionId.trim().isEmpty();
    }

    /**
     * Custom toString for logging (masks phone number for privacy)
     *
     * SECURITY: Never log full phone numbers in production
     * Format: +256700***456 or 0700***456
     *
     * @return Masked string representation
     */
    @Override
    public String toString() {
        String maskedPhone = maskPhoneNumber(phoneNumber);
        String maskedSession = sessionId != null ?
                sessionId.substring(0, Math.min(10, sessionId.length())) + "***" : "null";

        return "UssdLoginRequest{" +
                "phoneNumber='" + maskedPhone + '\'' +
                ", sessionId='" + maskedSession + '\'' +
                ", menuLevel='" + menuLevel + '\'' +
                '}';
    }

    /**
     * Masks phone number for secure logging
     * Shows first 3 and last 3 digits, masks middle digits
     *
     * @param phone Phone number to mask
     * @return Masked phone number (e.g., +256***456)
     */
    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 6) {
            return "***";
        }

        String cleaned = phone.replaceAll("[\\s\\-\\(\\)\\.]", "");

        if (cleaned.length() >= 6) {
            return cleaned.substring(0, 4) + "***" + cleaned.substring(cleaned.length() - 3);
        }

        return "***";
    }
}