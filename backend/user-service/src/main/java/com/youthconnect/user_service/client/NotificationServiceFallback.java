package com.youthconnect.user_service.client;

import com.youthconnect.user_service.dto.request.SmsRequest;
import com.youthconnect.user_service.dto.request.UssdConfirmationRequest;
import com.youthconnect.user_service.dto.request.WelcomeNotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fallback implementation for NotificationServiceClient
 * Provides graceful degradation when notification service is unavailable
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Slf4j
@Component
public class NotificationServiceFallback implements NotificationServiceClient {

    @Override
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendWelcomeNotification(
            WelcomeNotificationRequest request) {
        log.warn("Notification service unavailable - welcome notification fallback for user: {}",
                request.getUserId());

        return CompletableFuture.completedFuture(ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Notification service unavailable",
                "fallback", true
        )));
    }

    @Override
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendUssdConfirmation(
            UssdConfirmationRequest request) {
        log.warn("Notification service unavailable - USSD confirmation fallback for: {}",
                request.getPhoneNumber());

        return CompletableFuture.completedFuture(ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Notification service unavailable",
                "fallback", true
        )));
    }

    @Override
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendSms(SmsRequest request) {
        log.warn("Notification service unavailable - SMS fallback for: {}",
                request.getPhoneNumber());

        return CompletableFuture.completedFuture(ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Notification service unavailable",
                "fallback", true
        )));
    }
}