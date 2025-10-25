package com.youthconnect.notification.service.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * NOTIFICATION UTILITIES - HELPER METHODS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Collection of utility methods for notification processing:
 * - Template variable replacement
 * - Message truncation for SMS
 * - Timestamp formatting
 * - Email validation
 * - Text sanitization
 * - Multi-language support helpers
 *
 * @author Douglas Kings Kato
 * @version 1.0
 * @since 2025-01-20
 */
@Slf4j
public class NotificationUtils {

    // ═══════════════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Single SMS character limit (GSM-7 encoding).
     */
    public static final int SMS_SINGLE_LENGTH = 160;

    /**
     * Concatenated SMS character limit (GSM-7 encoding).
     */
    public static final int SMS_CONCATENATED_LENGTH = 306;

    /**
     * Email subject maximum length.
     */
    public static final int EMAIL_SUBJECT_MAX_LENGTH = 100;

    /**
     * East Africa Time zone.
     */
    public static final ZoneId EAT_ZONE = ZoneId.of("Africa/Nairobi");

    /**
     * Standard date-time format for notifications.
     * Example: 2025-01-20 14:30
     */
    public static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Friendly date-time format.
     * Example: Jan 20, 2025 at 2:30 PM
     */
    public static final DateTimeFormatter FRIENDLY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' h:mm a");

    /**
     * Template variable pattern: {{VARIABLE_NAME}}
     */
    private static final Pattern TEMPLATE_VARIABLE_PATTERN =
            Pattern.compile("\\{\\{([A-Z_]+)\\}\\}");

    /**
     * Email validation regex (RFC 5322 simplified).
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // ═══════════════════════════════════════════════════════════════════════
    // TEMPLATE PROCESSING
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Replace template variables with actual values.
     *
     * Template format: "Hello {{FIRST_NAME}}, your application for {{OPPORTUNITY_TITLE}} was received."
     * Variables map: {"FIRST_NAME": "John", "OPPORTUNITY_TITLE": "Youth Grant"}
     * Result: "Hello John, your application for Youth Grant was received."
     *
     * @param template Template string with {{VARIABLES}}
     * @param variables Map of variable names to values
     * @return Processed template with replaced variables
     */
    public static String replaceTemplateVariables(String template, Map<String, String> variables) {
        if (template == null || template.isBlank()) {
            return template;
        }

        if (variables == null || variables.isEmpty()) {
            return template;
        }

        String result = template;
        Matcher matcher = TEMPLATE_VARIABLE_PATTERN.matcher(template);

        while (matcher.find()) {
            String variableName = matcher.group(1);
            String variableValue = variables.getOrDefault(variableName, "");

            result = result.replace("{{" + variableName + "}}", variableValue);
        }

        return result;
    }

    /**
     * Build common notification variables map.
     *
     * @param firstName User's first name
     * @param opportunityTitle Opportunity title (optional)
     * @param webUrl Platform web URL
     * @return Map of standard template variables
     */
    public static Map<String, String> buildCommonVariables(
            String firstName,
            String opportunityTitle,
            String webUrl) {

        Map<String, String> variables = new HashMap<>();

        variables.put("FIRST_NAME", firstName != null ? firstName : "User");
        variables.put("OPPORTUNITY_TITLE", opportunityTitle != null ? opportunityTitle : "");
        variables.put("WEB_URL", webUrl != null ? webUrl : "https://kwetuhub.ug");
        variables.put("PLATFORM_NAME", "Kwetu-Hub Uganda");
        variables.put("CURRENT_YEAR", String.valueOf(LocalDateTime.now().getYear()));

        return variables;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SMS PROCESSING
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Truncate message to SMS character limit.
     *
     * Preserves whole words when possible.
     * Adds "..." if truncated.
     *
     * @param message Original message
     * @param maxLength Maximum length (160 or 306)
     * @return Truncated message
     */
    public static String truncateForSms(String message, int maxLength) {
        if (message == null) {
            return "";
        }

        if (message.length() <= maxLength) {
            return message;
        }

        // Reserve 3 characters for "..."
        int truncateAt = maxLength - 3;

        // Find last space before truncation point to preserve words
        int lastSpace = message.lastIndexOf(' ', truncateAt);

        if (lastSpace > 0 && lastSpace > (truncateAt - 20)) {
            return message.substring(0, lastSpace) + "...";
        }

        return message.substring(0, truncateAt) + "...";
    }

    /**
     * Calculate number of SMS segments required for message.
     *
     * SMS Segment Rules:
     * - Single SMS: 160 characters (GSM-7)
     * - Concatenated SMS: 153 characters per segment (7 chars for header)
     *
     * @param message Message content
     * @return Number of SMS segments required
     */
    public static int calculateSmsSegments(String message) {
        if (message == null || message.isEmpty()) {
            return 0;
        }

        int length = message.length();

        if (length <= SMS_SINGLE_LENGTH) {
            return 1;
        }

        // Concatenated SMS: 153 chars per segment
        return (int) Math.ceil((double) length / 153);
    }

    /**
     * Check if message fits in single SMS.
     *
     * @param message Message content
     * @return true if fits in 160 characters
     */
    public static boolean fitsInSingleSms(String message) {
        return message != null && message.length() <= SMS_SINGLE_LENGTH;
    }

    /**
     * Sanitize message for SMS (remove special characters that cause issues).
     *
     * Removes:
     * - Control characters
     * - Emojis (optional)
     * - Non-printable characters
     *
     * @param message Original message
     * @param removeEmojis Whether to remove emoji characters
     * @return Sanitized message
     */
    public static String sanitizeForSms(String message, boolean removeEmojis) {
        if (message == null) {
            return "";
        }

        String sanitized = message;

        // Remove control characters
        sanitized = sanitized.replaceAll("\\p{Cntrl}", "");

        // Remove non-printable characters
        sanitized = sanitized.replaceAll("[^\\p{Print}]", "");

        // Remove emojis if requested
        if (removeEmojis) {
            sanitized = sanitized.replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "");
        }

        return sanitized.trim();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EMAIL PROCESSING
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Validate email address format.
     *
     * @param email Email address to validate
     * @return true if valid email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Truncate email subject to maximum length.
     *
     * @param subject Original subject
     * @return Truncated subject (max 100 chars)
     */
    public static String truncateEmailSubject(String subject) {
        if (subject == null) {
            return "";
        }

        if (subject.length() <= EMAIL_SUBJECT_MAX_LENGTH) {
            return subject;
        }

        return subject.substring(0, EMAIL_SUBJECT_MAX_LENGTH - 3) + "...";
    }

    /**
     * Sanitize text for HTML display (prevent XSS).
     *
     * Escapes:
     * - < to &lt;
     * - > to &gt;
     * - & to &amp;
     * - " to &quot;
     * - ' to &#39;
     *
     * @param text Raw text
     * @return HTML-safe text
     */
    public static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TIMESTAMP FORMATTING
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Format timestamp in East Africa Time.
     *
     * @param dateTime Timestamp to format
     * @return Formatted string: "2025-01-20 14:30"
     */
    public static String formatTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        return dateTime.atZone(EAT_ZONE).format(DATETIME_FORMATTER);
    }

    /**
     * Format timestamp in friendly format.
     *
     * @param dateTime Timestamp to format
     * @return Formatted string: "Jan 20, 2025 at 2:30 PM"
     */
    public static String formatFriendlyTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        return dateTime.atZone(EAT_ZONE).format(FRIENDLY_FORMATTER);
    }

    /**
     * Get relative time description.
     *
     * Examples:
     * - "Just now" (< 1 minute)
     * - "5 minutes ago"
     * - "2 hours ago"
     * - "Yesterday"
     * - "3 days ago"
     *
     * @param dateTime Timestamp to describe
     * @return Relative time description
     */
    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now(EAT_ZONE);
        long secondsAgo = java.time.Duration.between(dateTime, now).getSeconds();

        if (secondsAgo < 60) {
            return "Just now";
        }

        long minutesAgo = secondsAgo / 60;
        if (minutesAgo < 60) {
            return minutesAgo + (minutesAgo == 1 ? " minute ago" : " minutes ago");
        }

        long hoursAgo = minutesAgo / 60;
        if (hoursAgo < 24) {
            return hoursAgo + (hoursAgo == 1 ? " hour ago" : " hours ago");
        }

        long daysAgo = hoursAgo / 24;
        if (daysAgo == 1) {
            return "Yesterday";
        }

        if (daysAgo < 7) {
            return daysAgo + " days ago";
        }

        // Fall back to formatted date for older timestamps
        return formatFriendlyTimestamp(dateTime);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MULTI-LANGUAGE SUPPORT
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Get greeting message in specified language.
     *
     * @param language Language code (EN, LG, LUG, ALU)
     * @param firstName User's first name
     * @return Localized greeting
     */
    public static String getGreeting(String language, String firstName) {
        String name = firstName != null ? firstName : "";

        return switch (language != null ? language.toUpperCase() : "EN") {
            case "LG" -> "Ki kati " + name + "!";        // Luganda
            case "LUG" -> "Candiru " + name + "!";       // Lugbara
            case "ALU" -> "Pito " + name + "!";          // Alur
            default -> "Hello " + name + "!";            // English
        };
    }

    /**
     * Get platform name in specified language.
     *
     * @param language Language code
     * @return Localized platform name
     */
    public static String getPlatformName(String language) {
        return switch (language != null ? language.toUpperCase() : "EN") {
            case "LG" -> "Kwetu-Hub Uganda";
            case "LUG" -> "Kwetu-Hub Uganda";
            case "ALU" -> "Kwetu-Hub Uganda";
            default -> "Kwetu-Hub Uganda";
        };
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TEXT PROCESSING
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Extract first N words from text.
     *
     * @param text Text to process
     * @param wordCount Number of words to extract
     * @return First N words
     */
    public static String extractFirstWords(String text, int wordCount) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String[] words = text.split("\\s+");

        if (words.length <= wordCount) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < wordCount; i++) {
            result.append(words[i]);
            if (i < wordCount - 1) {
                result.append(" ");
            }
        }

        return result.toString() + "...";
    }

    /**
     * Count words in text.
     *
     * @param text Text to analyze
     * @return Word count
     */
    public static int countWords(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        return text.trim().split("\\s+").length;
    }

    /**
     * Capitalize first letter of each word.
     *
     * @param text Text to capitalize
     * @return Title case text
     */
    public static String toTitleCase(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
                result.append(" ");
            }
        }

        return result.toString().trim();
    }
}