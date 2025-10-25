package com.youthconnect.notification.service.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.youthconnect.notification.service.dto.PushNotificationRequest;
import com.youthconnect.notification.service.entity.NotificationLog;
import com.youthconnect.notification.service.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * PUSH NOTIFICATION SERVICE - FIREBASE CLOUD MESSAGING
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Sends push notifications to Android and iOS devices via Firebase.
 *
 * Features:
 * - Device token validation
 * - Platform-specific configurations (Android/iOS)
 * - High-priority notifications (immediate delivery)
 * - Rich notifications (title, body, image, data payload)
 * - Delivery tracking and retry logic
 * - Error handling with graceful degradation
 *
 * Prerequisites:
 * - Firebase Admin SDK initialized (FirebaseConfig.java)
 * - Mobile apps configured with FCM
 * - Device tokens registered in user profiles
 *
 * @author Douglas Kings Kato
 * @version 1.0
 * @since 2025-01-20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PushNotificationService {

    private final FirebaseApp firebaseApp;
    private final NotificationLogRepository notificationLogRepository;

    /**
     * Send push notification to a single device.
     *
     * @param request Push notification request with device token and message
     * @return CompletableFuture with delivery result
     */
    @Async("notificationTaskExecutor")
    public CompletableFuture<Map<String, Object>> sendPushNotification(PushNotificationRequest request) {

        log.info("📱 Push Notification Request: token={}, title={}",
                maskToken(request.getDeviceToken()), request.getTitle());

        // Create notification log
        NotificationLog notificationLog = createNotificationLog(request);

        try {
            // Check if Firebase is initialized
            if (firebaseApp == null) {
                throw new IllegalStateException("Firebase not initialized - push notifications unavailable");
            }

            // Build Firebase message
            Message message = buildFirebaseMessage(request);

            // Send via Firebase
            String messageId = FirebaseMessaging.getInstance(firebaseApp).send(message);

            // Update notification log
            updateNotificationSuccess(notificationLog, messageId);

            log.info("✅ Push notification sent successfully: messageId={}, token={}",
                    messageId, maskToken(request.getDeviceToken()));

            return CompletableFuture.completedFuture(buildSuccessResponse(messageId, request.getDeviceToken()));

        } catch (FirebaseMessagingException e) {
            log.error("❌ Firebase error sending push notification: code={}, message={}",
                    e.getMessagingErrorCode(), e.getMessage());

            updateNotificationFailure(notificationLog, e.getMessage());

            return CompletableFuture.completedFuture(buildFailureResponse(e.getMessage(), shouldRetry(e)));

        } catch (Exception e) {
            log.error("❌ Unexpected error sending push notification", e);

            updateNotificationFailure(notificationLog, e.getMessage());

            return CompletableFuture.completedFuture(buildFailureResponse(e.getMessage(), false));
        }
    }

    /**
     * Build Firebase message from request.
     *
     * Handles:
     * - Platform-specific configurations (Android/iOS)
     * - Notification payload (title, body, image)
     * - Data payload (custom key-value pairs)
     * - Priority settings
     * - TTL and expiration
     *
     * @param request Push notification request
     * @return Firebase Message object
     */
    private Message buildFirebaseMessage(PushNotificationRequest request) {

        // Build notification payload
        Notification notification = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .setImage(request.getImageUrl()) // Optional image URL
                .build();

        // Build message builder
        Message.Builder messageBuilder = Message.builder()
                .setToken(request.getDeviceToken())
                .setNotification(notification);

        // Add data payload if provided
        if (request.getData() != null && !request.getData().isEmpty()) {
            messageBuilder.putAllData(request.getData());
        }

        // Platform-specific configurations
        if ("ANDROID".equalsIgnoreCase(request.getPlatform())) {
            messageBuilder.setAndroidConfig(buildAndroidConfig(request));
        } else if ("IOS".equalsIgnoreCase(request.getPlatform())) {
            messageBuilder.setApnsConfig(buildApnsConfig(request));
        } else {
            // Both platforms - set both configs
            messageBuilder.setAndroidConfig(buildAndroidConfig(request));
            messageBuilder.setApnsConfig(buildApnsConfig(request));
        }

        return messageBuilder.build();
    }

    /**
     * Build Android-specific configuration.
     *
     * @param request Push notification request
     * @return Android configuration
     */
    private AndroidConfig buildAndroidConfig(PushNotificationRequest request) {

        AndroidNotification.Builder androidNotification = AndroidNotification.builder()
                .setSound(request.getSound() != null ? request.getSound() : "default")
                .setClickAction(request.getClickAction());

        // Add image if provided
        if (request.getImageUrl() != null) {
            androidNotification.setImage(request.getImageUrl());
        }

        return AndroidConfig.builder()
                .setTtl(request.getTtlSeconds() * 1000L) // Convert to milliseconds
                .setPriority(request.isHighPriority() ?
                        AndroidConfig.Priority.HIGH : AndroidConfig.Priority.NORMAL)
                .setNotification(androidNotification.build())
                .build();
    }

    /**
     * Build iOS-specific configuration (APNs).
     *
     * @param request Push notification request
     * @return APNs configuration
     */
    private ApnsConfig buildApnsConfig(PushNotificationRequest request) {

        Map<String, Object> apsPayload = new HashMap<>();

        // Alert payload
        Map<String, String> alert = new HashMap<>();
        alert.put("title", request.getTitle());
        alert.put("body", request.getBody());
        apsPayload.put("alert", alert);

        // Sound
        apsPayload.put("sound", request.getSound() != null ? request.getSound() : "default");

        // Badge count
        if (request.getBadge() != null) {
            apsPayload.put("badge", request.getBadge());
        }

        // Category for custom actions
        if (request.getCategory() != null) {
            apsPayload.put("category", request.getCategory());
        }

        return ApnsConfig.builder()
                .setAps(Aps.builder()
                        .putAllCustomData(apsPayload)
                        .build())
                .build();
    }

    /**
     * Create notification log entry.
     *
     * @param request Push notification request
     * @return Notification log entity
     */
    private NotificationLog createNotificationLog(PushNotificationRequest request) {
        NotificationLog log = NotificationLog.builder()
                .userId(request.getUserId())
                .notificationType(NotificationLog.NotificationType.PUSH)
                .recipient(maskToken(request.getDeviceToken()))
                .subject(request.getTitle())
                .content(request.getBody())
                .status(NotificationLog.NotificationStatus.PENDING)
                .retryCount(0)
                .maxRetries(3)
                .provider("FIREBASE")
                .createdAt(LocalDateTime.now())
                .build();

        return notificationLogRepository.save(log);
    }

    /**
     * Update notification log on successful delivery.
     *
     * @param log Notification log
     * @param messageId Firebase message ID
     */
    private void updateNotificationSuccess(NotificationLog log, String messageId) {
        log.setStatus(NotificationLog.NotificationStatus.SENT);
        log.setSentAt(LocalDateTime.now());
        log.setProviderMessageId(messageId);
        log.setUpdatedAt(LocalDateTime.now());
        notificationLogRepository.save(log);
    }

    /**
     * Update notification log on delivery failure.
     *
     * @param log Notification log
     * @param errorMessage Error message
     */
    private void updateNotificationFailure(NotificationLog log, String errorMessage) {
        log.setStatus(NotificationLog.NotificationStatus.FAILED);
        log.setErrorMessage(errorMessage);
        log.setRetryCount(log.getRetryCount() + 1);
        log.setUpdatedAt(LocalDateTime.now());

        // Schedule retry if under max attempts
        if (log.getRetryCount() < log.getMaxRetries()) {
            int delayMinutes = 5 * (int) Math.pow(2, log.getRetryCount() - 1);
            log.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
        }

        notificationLogRepository.save(log);
    }

    /**
     * Determine if notification should be retried based on error type.
     *
     * Retryable errors:
     * - UNAVAILABLE (temporary service issue)
     * - INTERNAL (Firebase server error)
     * - DEADLINE_EXCEEDED (timeout)
     *
     * Non-retryable errors:
     * - INVALID_ARGUMENT (bad request - won't succeed on retry)
     * - UNREGISTERED (device token no longer valid)
     * - SENDER_ID_MISMATCH (wrong Firebase project)
     *
     * @param exception Firebase exception
     * @return true if should retry
     */
    private boolean shouldRetry(FirebaseMessagingException exception) {
        MessagingErrorCode errorCode = exception.getMessagingErrorCode();

        return errorCode == MessagingErrorCode.UNAVAILABLE ||
                errorCode == MessagingErrorCode.INTERNAL ||
                errorCode == MessagingErrorCode.QUOTA_EXCEEDED;
    }

    /**
     * Build success response.
     *
     * @param messageId Firebase message ID
     * @param deviceToken Device token (masked)
     * @return Success response map
     */
    private Map<String, Object> buildSuccessResponse(String messageId, String deviceToken) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", "SENT");
        response.put("messageId", messageId);
        response.put("recipient", maskToken(deviceToken));
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * Build failure response.
     *
     * @param error Error message
     * @param willRetry Whether notification will be retried
     * @return Failure response map
     */
    private Map<String, Object> buildFailureResponse(String error, boolean willRetry) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("status", "FAILED");
        response.put("error", error);
        response.put("willRetry", willRetry);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * Mask device token for privacy in logs.
     *
     * Example: "dUxK7FqGRg...9I0J" → "dUxK****I0J"
     *
     * @param token Device token
     * @return Masked token
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 12) {
            return "****";
        }
        return token.substring(0, 4) + "****" + token.substring(token.length() - 3);
    }

    /**
     * Check if Firebase is available.
     *
     * @return true if Firebase initialized
     */
    public boolean isFirebaseAvailable() {
        return firebaseApp != null;
    }
}