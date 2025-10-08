package com.youthconnect.ussd_service.util;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

/**
 * Utility class for phone number validation and formatting
 */
@Component
public class PhoneNumberValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");

    /**
     * Validates a phone number
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    /**
     * Normalizes a phone number to a standard format
     */
    public String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        // Remove all non-digit characters except leading +
        String normalized = phoneNumber.replaceAll("[^0-9+]", "");

        // If it starts with +, keep it, otherwise ensure it has country code
        if (normalized.startsWith("+")) {
            return normalized;
        }

        // Add Ugandan country code if missing
        if (normalized.startsWith("0")) {
            return "+256" + normalized.substring(1);
        }

        if (normalized.length() == 9 && !normalized.startsWith("0")) {
            return "+256" + normalized;
        }

        return "+" + normalized;
    }

    /**
     * Extracts the local number part (without country code)
     */
    public String getLocalNumber(String phoneNumber) {
        String normalized = normalizePhoneNumber(phoneNumber);
        if (normalized.startsWith("+256")) {
            return "0" + normalized.substring(4);
        }
        return normalized;
    }
}