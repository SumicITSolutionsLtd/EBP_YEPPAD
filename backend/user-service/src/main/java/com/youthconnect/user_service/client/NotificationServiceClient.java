package com.youthconnect.user_service.client;

import com.youthconnect.user_service.dto.request.SmsRequest;
import com.youthconnect.user_service.dto.request.UssdConfirmationRequest;
import com.youthconnect.user_service.dto.request.WelcomeNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Client for integrating with Notification Service
 * Handles SMS and email notifications
 */
@FeignClient(name = "notification-service", fallback = NotificationServiceFallback.class)
public interface NotificationServiceClient {

    @PostMapping("/api/notifications/welcome")
    CompletableFuture<ResponseEntity<Map<String, Object>>> sendWelcomeNotification(
            @RequestBody WelcomeNotificationRequest request);

    @PostMapping("/api/notifications/ussd/registration")
    CompletableFuture<ResponseEntity<Map<String, Object>>> sendUssdConfirmation(
            @RequestBody UssdConfirmationRequest request);

    @PostMapping("/api/notifications/sms/send")
    CompletableFuture<ResponseEntity<Map<String, Object>>> sendSms(
            @RequestBody SmsRequest request);
}