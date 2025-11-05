package com.youthconnect.notification.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * PUSH NOTIFICATION REQUEST DTO
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Request object for sending push notifications via Firebase Cloud Messaging.
 *
 * Use Cases:
 * - Application status updates
 * - New opportunity alerts
 * - Mentorship session reminders
 * - Real-time platform notifications
 *
 * Example Usage:
 * <pre>
 * PushNotificationRequest request = PushNotificationRequest.builder()
 *     .deviceToken("firebase_device_token_here")
 *     .title("New Opportunity!")
 *     .body("A grant matching your profile is now available")
 *     .data(Map.of("opportunityId", "123", "type", "GRANT"))
 *     .highPriority(true)
 *     .build();
 * </pre>
 *
 * @author Douglas Kings Kato
 * @version 1.0
 * @since 2025-10-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationRequest {

    /**
     * Firebase device token (FCM registration token).
     * Obtained from mobile app when user grants notification permission.
     *
     * Example: "dUxK7FqGRgqW8X9Y0Z1A2B3C4D5E6F7G8H9I0J"
     */
    @NotBlank(message = "Device token is required")
    private String deviceToken;

    /**
     * Notification title (shown in notification tray).
     *
     * Best Practices:
     * - Keep under 50 characters
     * - Clear and actionable
     * - Avoid special characters
     *
     * Example: "Application Approved!"
     */
    @NotBlank(message = "Notification title is required")
    private String title;

    /**
     * Notification body text.
     *
     * Best Practices:
     * - Keep under 150 characters
     * - Provide context and action
     * - Use clear language
     *
     * Example: "Your grant application has been approved. Check your dashboard for details."
     */
    @NotBlank(message = "Notification body is required")
    private String body;

    /**
     * Image URL to display in notification (optional).
     *
     * Requirements:
     * - HTTPS URL
     * - PNG or JPEG format
     * - Recommended: 1024x512px
     * - Max size: 1MB
     *
     * Example: "https://cdn.youthconnect.ug/images/opportunity_123.jpg"
     */
    private String imageUrl;

    /**
     * Additional data payload (key-value pairs).
     * Used by mobile app to handle notification clicks.
     *
     * Common Keys:
     * - "screen": Target screen in app
     * - "id": Entity ID (opportunity, application, etc.)
     * - "type": Entity type
     * - "action": Action to perform
     *
     * Example:
     * {
     *   "screen": "opportunity_details",
     *   "opportunityId": "123",
     *   "type": "GRANT"
     * }
     */
    private Map<String, String> data;

    /**
     * Platform-specific notification (ANDROID, IOS, null for both).
     *
     * Options:
     * - "ANDROID": Android devices only
     * - "IOS": iOS devices only
     * - null: Both platforms
     */
    private String platform;

    /**
     * High priority notification (delivers immediately, wakes device).
     *
     * When to use:
     * - TRUE: Urgent notifications (deadline reminders, approvals)
     * - FALSE: Non-urgent updates (daily digests, tips)
     *
     * Default: false
     */
    @Builder.Default
    private boolean highPriority = false;

    /**
     * Click action (activity to open on Android).
     *
     * Example: "FLUTTER_NOTIFICATION_CLICK"
     */
    private String clickAction;

    /**
     * Notification category for iOS.
     *
     * Example: "OPPORTUNITY_ALERT"
     */
    private String category;

    /**
     * User ID (for logging purposes).
     */
    private Long userId;

    /**
     * Time-to-live in seconds (how long FCM should store if device offline).
     *
     * Recommendations:
     * - Time-sensitive: 3600 (1 hour)
     * - General updates: 86400 (24 hours)
     * - Marketing: 259200 (3 days)
     *
     * Default: 24 hours
     */
    @Builder.Default
    private Integer ttlSeconds = 86400;

    /**
     * Custom sound file name (without extension).
     *
     * Requirements:
     * - File must exist in mobile app bundle
     * - Android: In /res/raw/
     * - iOS: In app bundle
     *
     * Example: "notification_sound" (for notification_sound.mp3)
     * Default: "default"
     */
    @Builder.Default
    private String sound = "default";

    /**
     * Badge count for iOS (number shown on app icon).
     *
     * Set to null to not update badge.
     */
    private Integer badge;
}