package com.youthconnect.notification.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.Set;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * NOTIFICATION PREFERENCES DTO
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * User-configurable notification preferences controlling:
 * - Which channels to receive notifications (SMS, Email, Push, In-app)
 * - Notification frequency (immediate, daily digest, weekly)
 * - Quiet hours (no SMS notifications during sleep)
 * - Notification categories (updates, marketing, reminders, alerts)
 *
 * Use Cases:
 * - User updates preferences via settings page
 * - Notification service checks preferences before sending
 * - Reduces notification fatigue
 * - Complies with user consent requirements
 * - GDPR/privacy compliance
 *
 * Example Usage:
 * <pre>
 * NotificationPreferences prefs = NotificationPreferences.builder()
 *     .userId(123L)
 *     .smsEnabled(true)
 *     .emailEnabled(true)
 *     .pushEnabled(false)
 *     .frequency(NotificationFrequency.IMMEDIATE)
 *     .quietHoursStart(LocalTime.of(22, 0))  // 10 PM
 *     .quietHoursEnd(LocalTime.of(7, 0))     // 7 AM
 *     .enabledCategories(Set.of(
 *         NotificationCategory.UPDATES,
 *         NotificationCategory.ALERTS
 *     ))
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
public class NotificationPreferences {

    /**
     * User ID (foreign key to users table).
     */
    @NotNull(message = "User ID is required")
    private Long userId;

    // ═══════════════════════════════════════════════════════════════════════
    // CHANNEL PREFERENCES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * SMS notifications enabled.
     *
     * When false:
     * - No SMS will be sent to this user
     * - Exceptions: Critical security alerts (password reset, login alerts)
     * - Cost consideration: SMS costs ~UGX 60 each
     */
    @Builder.Default
    private boolean smsEnabled = true;

    /**
     * Email notifications enabled.
     *
     * When false:
     * - No emails will be sent
     * - Exceptions: Account-related emails (verification, password reset)
     * - Includes unsubscribe link in all marketing emails
     */
    @Builder.Default
    private boolean emailEnabled = true;

    /**
     * Push notifications enabled (mobile app).
     *
     * When false:
     * - No push notifications sent to mobile devices
     * - Exceptions: Critical app updates
     * - Requires device FCM token to be registered
     */
    @Builder.Default
    private boolean pushEnabled = true;

    /**
     * In-app notifications enabled (web dashboard).
     *
     * When false:
     * - Notifications not shown in web interface
     * - Still logged in database for history
     */
    @Builder.Default
    private boolean inAppEnabled = true;

    // ═══════════════════════════════════════════════════════════════════════
    // FREQUENCY PREFERENCES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Notification delivery frequency.
     *
     * Options:
     * - IMMEDIATE: Send notifications as they occur (default)
     * - DAILY_DIGEST: Batch notifications sent once daily at 8 AM
     * - WEEKLY_DIGEST: Batch notifications sent every Monday at 8 AM
     *
     * Applies to non-urgent notifications only.
     * Urgent notifications (deadlines, approvals) always sent immediately.
     */
    @Builder.Default
    private NotificationFrequency frequency = NotificationFrequency.IMMEDIATE;

    /**
     * Preferred digest delivery time (for DAILY/WEEKLY frequency).
     *
     * Default: 08:00 (8 AM East Africa Time)
     *
     * Example: User wants daily digest at 6 PM
     * digestTime = LocalTime.of(18, 0)
     */
    @Builder.Default
    private LocalTime digestTime = LocalTime.of(8, 0);

    // ═══════════════════════════════════════════════════════════════════════
    // QUIET HOURS (DO NOT DISTURB)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Quiet hours enabled.
     *
     * When true:
     * - SMS notifications suppressed during quiet hours
     * - Push notifications silenced (no sound/vibration)
     * - Urgent notifications still delivered but silently
     */
    @Builder.Default
    private boolean quietHoursEnabled = false;

    /**
     * Quiet hours start time.
     *
     * Default: 22:00 (10 PM)
     *
     * Example: Sleep time starts at 11 PM
     * quietHoursStart = LocalTime.of(23, 0)
     */
    @Builder.Default
    private LocalTime quietHoursStart = LocalTime.of(22, 0);

    /**
     * Quiet hours end time.
     *
     * Default: 07:00 (7 AM)
     *
     * Example: Wake up time is 6 AM
     * quietHoursEnd = LocalTime.of(6, 0)
     */
    @Builder.Default
    private LocalTime quietHoursEnd = LocalTime.of(7, 0);

    // ═══════════════════════════════════════════════════════════════════════
    // CATEGORY PREFERENCES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Enabled notification categories.
     *
     * User can opt-out of specific categories:
     * - UPDATES: Application status, opportunity updates
     * - ALERTS: Deadlines, urgent notifications
     * - REMINDERS: Mentorship sessions, learning progress
     * - MARKETING: New features, platform updates, tips
     * - SOCIAL: Comments, forum replies, community activity
     *
     * Empty set = all categories disabled (not recommended)
     * Null = all categories enabled (default)
     */
    @Builder.Default
    private Set<NotificationCategory> enabledCategories = Set.of(
            NotificationCategory.UPDATES,
            NotificationCategory.ALERTS,
            NotificationCategory.REMINDERS
    );

    // ═══════════════════════════════════════════════════════════════════════
    // LANGUAGE PREFERENCE
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Preferred notification language.
     *
     * Options: EN, LG, LUG, ALU
     * Default: EN (English)
     *
     * Affects:
     * - SMS message language
     * - Email template language
     * - Push notification text language
     */
    @Builder.Default
    private String preferredLanguage = "EN";

    // ═══════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Check if SMS channel is enabled for a specific category.
     *
     * @param category Notification category
     * @return true if SMS enabled and category enabled
     */
    public boolean isSmsEnabledForCategory(NotificationCategory category) {
        return smsEnabled && isCategoryEnabled(category);
    }

    /**
     * Check if email channel is enabled for a specific category.
     *
     * @param category Notification category
     * @return true if email enabled and category enabled
     */
    public boolean isEmailEnabledForCategory(NotificationCategory category) {
        return emailEnabled && isCategoryEnabled(category);
    }

    /**
     * Check if push channel is enabled for a specific category.
     *
     * @param category Notification category
     * @return true if push enabled and category enabled
     */
    public boolean isPushEnabledForCategory(NotificationCategory category) {
        return pushEnabled && isCategoryEnabled(category);
    }

    /**
     * Check if a notification category is enabled.
     *
     * @param category Notification category
     * @return true if category is in enabled list (or list is null/empty = all enabled)
     */
    public boolean isCategoryEnabled(NotificationCategory category) {
        // Null or empty = all categories enabled
        if (enabledCategories == null || enabledCategories.isEmpty()) {
            return true;
        }
        return enabledCategories.contains(category);
    }

    /**
     * Check if current time is within quiet hours.
     *
     * Handles overnight quiet hours (e.g., 10 PM to 7 AM).
     *
     * @param currentTime Time to check
     * @return true if time falls within quiet hours
     */
    public boolean isWithinQuietHours(LocalTime currentTime) {
        if (!quietHoursEnabled) {
            return false;
        }

        // Handle overnight quiet hours (e.g., 22:00 to 07:00)
        if (quietHoursStart.isAfter(quietHoursEnd)) {
            return currentTime.isAfter(quietHoursStart) || currentTime.isBefore(quietHoursEnd);
        }

        // Handle same-day quiet hours (e.g., 01:00 to 05:00)
        return currentTime.isAfter(quietHoursStart) && currentTime.isBefore(quietHoursEnd);
    }

    /**
     * Check if any channel is enabled.
     *
     * @return true if at least one notification channel is enabled
     */
    public boolean hasAnyChannelEnabled() {
        return smsEnabled || emailEnabled || pushEnabled || inAppEnabled;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Notification delivery frequency options.
     */
    public enum NotificationFrequency {
        /**
         * Send notifications immediately as they occur.
         */
        IMMEDIATE,

        /**
         * Batch and send once daily (default 8 AM).
         */
        DAILY_DIGEST,

        /**
         * Batch and send once weekly (every Monday 8 AM).
         */
        WEEKLY_DIGEST
    }

    /**
     * Notification category types.
     */
    public enum NotificationCategory {
        /**
         * Application status updates, opportunity updates.
         */
        UPDATES,

        /**
         * Deadlines, urgent actions required.
         */
        ALERTS,

        /**
         * Mentorship sessions, learning progress, saved items.
         */
        REMINDERS,

        /**
         * New features, platform updates, tips, newsletters.
         */
        MARKETING,

        /**
         * Comments, forum replies, community activity.
         */
        SOCIAL
    }
}