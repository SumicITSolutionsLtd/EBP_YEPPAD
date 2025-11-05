package com.youthconnect.notification.service.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * UGANDA PHONE NUMBER VALIDATOR & FORMATTER
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Validates and formats Ugandan mobile phone numbers for SMS delivery.
 *
 * **Uganda Phone Number Format:**
 * - Country Code: +256
 * - Mobile Operators:
 *   - MTN: 077x, 078x, 039x
 *   - Airtel: 070x, 075x
 *   - Africell: 079x
 * - Total Digits: 12 (including country code)
 * - Format: +256XXXXXXXXX
 *
 * **Accepted Input Formats:**
 * - International: +256701234567
 * - Local: 0701234567
 * - Without prefix: 701234567
 *
 * **Output Format:**
 * - Always returns: +256XXXXXXXXX
 *
 * **Usage Example:**
 * <pre>
 * // Validation
 * boolean isValid = PhoneNumberValidator.isValidUgandaPhone("0701234567");
 *
 * // Formatting
 * String formatted = PhoneNumberValidator.formatToInternational("0701234567");
 * // Returns: "+256701234567"
 *
 * // Extraction
 * String operator = PhoneNumberValidator.getOperator("+256701234567");
 * // Returns: "Airtel"
 * </pre>
 *
 * @author Douglas Kings Kato
 * @version 1.0
 * @since 2025-10-20
 */
@Slf4j
public class PhoneNumberValidator {

    // ═══════════════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Uganda country code.
     */
    private static final String UGANDA_COUNTRY_CODE = "256";

    /**
     * Uganda country code with plus sign.
     */
    private static final String UGANDA_CODE_WITH_PLUS = "+256";

    /**
     * Total length of Uganda phone number (including country code).
     * Format: +256XXXXXXXXX = 13 characters
     */
    private static final int FULL_LENGTH_WITH_PLUS = 13;

    /**
     * Total length without plus sign.
     * Format: 256XXXXXXXXX = 12 characters
     */
    private static final int FULL_LENGTH_WITHOUT_PLUS = 12;

    /**
     * Length of local format phone number.
     * Format: 0XXXXXXXXX = 10 characters
     */
    private static final int LOCAL_FORMAT_LENGTH = 10;

    /**
     * Length of phone number without prefix (local or country code).
     * Format: XXXXXXXXX = 9 characters
     */
    private static final int WITHOUT_PREFIX_LENGTH = 9;

    // ═══════════════════════════════════════════════════════════════════════
    // REGEX PATTERNS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Pattern for international format with plus.
     * Example: +256701234567
     */
    private static final Pattern INTERNATIONAL_WITH_PLUS = Pattern.compile(
            "^\\+256[0-9]{9}$"
    );

    /**
     * Pattern for international format without plus.
     * Example: 256701234567
     */
    private static final Pattern INTERNATIONAL_WITHOUT_PLUS = Pattern.compile(
            "^256[0-9]{9}$"
    );

    /**
     * Pattern for local format starting with 0.
     * Example: 0701234567
     */
    private static final Pattern LOCAL_FORMAT = Pattern.compile(
            "^0[7][0-9]{8}$"
    );

    /**
     * Pattern for format without any prefix.
     * Example: 701234567
     */
    private static final Pattern WITHOUT_PREFIX = Pattern.compile(
            "^[7][0-9]{8}$"
    );

    /**
     * Pattern for validating formatted Uganda phone numbers.
     * Must start with 07 (local format) or +2567 (international).
     */
    private static final Pattern VALID_UGANDA_PHONE = Pattern.compile(
            "^(\\+256|0)?7[0-9]{8}$"
    );

    // ═══════════════════════════════════════════════════════════════════════
    // MOBILE OPERATOR PREFIXES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * MTN Uganda prefixes (077, 078, 039).
     */
    private static final Pattern MTN_PATTERN = Pattern.compile("^(077|078|039)");

    /**
     * Airtel Uganda prefixes (070, 075).
     */
    private static final Pattern AIRTEL_PATTERN = Pattern.compile("^(070|075)");

    /**
     * Africell Uganda prefix (079).
     */
    private static final Pattern AFRICELL_PATTERN = Pattern.compile("^079");

    // ═══════════════════════════════════════════════════════════════════════
    // VALIDATION METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Validate if a phone number is a valid Ugandan mobile number.
     *
     * Accepts formats:
     * - +256701234567
     * - 256701234567
     * - 0701234567
     * - 701234567
     *
     * @param phoneNumber Phone number to validate
     * @return true if valid Uganda phone number
     */
    public static boolean isValidUgandaPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            log.debug("❌ Phone validation failed: null or blank");
            return false;
        }

        // Remove all non-numeric characters except +
        String cleaned = phoneNumber.replaceAll("[^+0-9]", "");

        log.debug("🔍 Validating phone: original={}, cleaned={}", phoneNumber, cleaned);

        // Check against all accepted patterns
        boolean isValid = INTERNATIONAL_WITH_PLUS.matcher(cleaned).matches() ||
                INTERNATIONAL_WITHOUT_PLUS.matcher(cleaned).matches() ||
                LOCAL_FORMAT.matcher(cleaned).matches() ||
                WITHOUT_PREFIX.matcher(cleaned).matches();

        log.debug("✅ Phone validation result: {} → {}", cleaned, isValid);

        return isValid;
    }

    /**
     * Check if phone number starts with valid Ugandan mobile prefix (07x).
     *
     * @param phoneNumber Phone number to check
     * @return true if starts with 07x prefix
     */
    public static boolean hasValidMobilePrefix(String phoneNumber) {
        String cleaned = cleanPhoneNumber(phoneNumber);

        // Convert to local format for prefix checking
        String localFormat = toLocalFormat(cleaned);

        return localFormat != null && localFormat.startsWith("07");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FORMATTING METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Format phone number to international format (+256XXXXXXXXX).
     *
     * Converts any valid format to: +256701234567
     *
     * @param phoneNumber Phone number in any valid format
     * @return Formatted phone number with +256 prefix
     * @throws IllegalArgumentException if phone number is invalid
     */
    public static String formatToInternational(String phoneNumber) {
        if (!isValidUgandaPhone(phoneNumber)) {
            throw new IllegalArgumentException(
                    "Invalid Uganda phone number format: " + phoneNumber
            );
        }

        String cleaned = cleanPhoneNumber(phoneNumber);

        // Already in international format with +
        if (cleaned.startsWith("+256")) {
            return cleaned;
        }

        // International format without +
        if (cleaned.startsWith("256")) {
            return "+" + cleaned;
        }

        // Local format with 0
        if (cleaned.startsWith("0")) {
            return "+256" + cleaned.substring(1);
        }

        // Without any prefix
        return "+256" + cleaned;
    }

    /**
     * Format phone number to local format (0XXXXXXXXX).
     *
     * Converts any valid format to: 0701234567
     *
     * @param phoneNumber Phone number in any valid format
     * @return Formatted phone number with 0 prefix
     * @throws IllegalArgumentException if phone number is invalid
     */
    public static String formatToLocal(String phoneNumber) {
        if (!isValidUgandaPhone(phoneNumber)) {
            throw new IllegalArgumentException(
                    "Invalid Uganda phone number format: " + phoneNumber
            );
        }

        String cleaned = cleanPhoneNumber(phoneNumber);

        // Already in local format
        if (cleaned.startsWith("0")) {
            return cleaned;
        }

        // International format with +256
        if (cleaned.startsWith("+256")) {
            return "0" + cleaned.substring(4);
        }

        // International format without +
        if (cleaned.startsWith("256")) {
            return "0" + cleaned.substring(3);
        }

        // Without any prefix
        return "0" + cleaned;
    }

    /**
     * Convert phone number to local format (private helper).
     *
     * @param cleaned Already cleaned phone number
     * @return Local format or null if invalid
     */
    private static String toLocalFormat(String cleaned) {
        try {
            if (cleaned.startsWith("0")) {
                return cleaned;
            }
            if (cleaned.startsWith("+256")) {
                return "0" + cleaned.substring(4);
            }
            if (cleaned.startsWith("256")) {
                return "0" + cleaned.substring(3);
            }
            return "0" + cleaned;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Clean phone number by removing non-numeric characters except +.
     *
     * @param phoneNumber Raw phone number
     * @return Cleaned phone number (only digits and +)
     */
    private static String cleanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return "";
        }
        return phoneNumber.replaceAll("[^+0-9]", "");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // OPERATOR DETECTION
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Get mobile operator name from phone number.
     *
     * Uganda Mobile Operators:
     * - MTN: 077x, 078x, 039x
     * - Airtel: 070x, 075x
     * - Africell: 079x
     *
     * @param phoneNumber Phone number (any valid format)
     * @return Operator name (MTN, Airtel, Africell) or UNKNOWN
     */
    public static String getOperator(String phoneNumber) {
        if (!isValidUgandaPhone(phoneNumber)) {
            return "UNKNOWN";
        }

        String cleaned = cleanPhoneNumber(phoneNumber);
        String localFormat = toLocalFormat(cleaned);

        if (localFormat == null) {
            return "UNKNOWN";
        }

        // Extract prefix (first 4 digits: 0 + 3 operator digits)
        String prefix = localFormat.substring(0, 4);

        if (MTN_PATTERN.matcher(prefix.substring(1)).find()) {
            return "MTN";
        }

        if (AIRTEL_PATTERN.matcher(prefix.substring(1)).find()) {
            return "Airtel";
        }

        if (AFRICELL_PATTERN.matcher(prefix.substring(1)).find()) {
            return "Africell";
        }

        return "UNKNOWN";
    }

    /**
     * Check if phone number is from MTN network.
     *
     * @param phoneNumber Phone number
     * @return true if MTN operator
     */
    public static boolean isMTN(String phoneNumber) {
        return "MTN".equals(getOperator(phoneNumber));
    }

    /**
     * Check if phone number is from Airtel network.
     *
     * @param phoneNumber Phone number
     * @return true if Airtel operator
     */
    public static boolean isAirtel(String phoneNumber) {
        return "Airtel".equals(getOperator(phoneNumber));
    }

    /**
     * Check if phone number is from Africell network.
     *
     * @param phoneNumber Phone number
     * @return true if Africell operator
     */
    public static boolean isAfricell(String phoneNumber) {
        return "Africell".equals(getOperator(phoneNumber));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MASKING FOR PRIVACY
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Mask phone number for privacy in logs and displays.
     *
     * Examples:
     * - +256701234567 → +256****4567
     * - 0701234567 → 070****567
     *
     * Shows first 4 digits, masks middle, shows last 4 digits.
     *
     * @param phoneNumber Phone number to mask
     * @return Masked phone number
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return "****";
        }

        String cleaned = cleanPhoneNumber(phoneNumber);

        if (cleaned.length() < 8) {
            return "****";
        }

        int keepStart = Math.min(4, cleaned.length() / 3);
        int keepEnd = Math.min(4, cleaned.length() / 3);

        return cleaned.substring(0, keepStart) +
                "****" +
                cleaned.substring(cleaned.length() - keepEnd);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BULK VALIDATION
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Validate multiple phone numbers at once.
     *
     * @param phoneNumbers Array of phone numbers
     * @return Array of validation results (same order as input)
     */
    public static boolean[] validateBulk(String[] phoneNumbers) {
        if (phoneNumbers == null) {
            return new boolean[0];
        }

        boolean[] results = new boolean[phoneNumbers.length];

        for (int i = 0; i < phoneNumbers.length; i++) {
            results[i] = isValidUgandaPhone(phoneNumbers[i]);
        }

        return results;
    }

    /**
     * Format multiple phone numbers to international format.
     *
     * @param phoneNumbers Array of phone numbers
     * @return Array of formatted phone numbers (null for invalid numbers)
     */
    public static String[] formatBulkToInternational(String[] phoneNumbers) {
        if (phoneNumbers == null) {
            return new String[0];
        }

        String[] formatted = new String[phoneNumbers.length];

        for (int i = 0; i < phoneNumbers.length; i++) {
            try {
                formatted[i] = formatToInternational(phoneNumbers[i]);
            } catch (IllegalArgumentException e) {
                formatted[i] = null;
                log.warn("Invalid phone number in bulk format: {}", phoneNumbers[i]);
            }
        }

        return formatted;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EXAMPLES FOR DOCUMENTATION
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Main method demonstrating validator usage (for testing only).
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        log.info("═══════════════════════════════════════════════════════");
        log.info("UGANDA PHONE NUMBER VALIDATOR - EXAMPLES");
        log.info("═══════════════════════════════════════════════════════");

        // Test cases
        String[] testNumbers = {
                "+256701234567",    // International with +
                "256701234567",     // International without +
                "0701234567",       // Local format
                "701234567",        // Without prefix
                "0771234567",       // MTN
                "0751234567",       // Airtel
                "0791234567",       // Africell
                "0123456789",       // Invalid prefix
                "07012",            // Too short
                null                // Null
        };

        for (String number : testNumbers) {
            log.info("\n--- Testing: {} ---", number);

            boolean isValid = isValidUgandaPhone(number);
            log.info("Valid: {}", isValid);

            if (isValid) {
                log.info("International: {}", formatToInternational(number));
                log.info("Local: {}", formatToLocal(number));
                log.info("Operator: {}", getOperator(number));
                log.info("Masked: {}", maskPhoneNumber(number));
            }
        }
    }
}