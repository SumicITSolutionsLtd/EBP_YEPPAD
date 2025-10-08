package com.youthconnect.notification.service.controller;

import com.youthconnect.notification.service.dto.EmailRequest;
import com.youthconnect.notification.service.dto.SmsRequest;
import com.youthconnect.notification.service.dto.UssdConfirmationRequest;
import com.youthconnect.notification.service.dto.WelcomeNotificationRequest;
import com.youthconnect.notification.service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for handling notification-related endpoints.
 * Supports SMS, Email, Welcome messages, and USSD confirmations.
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Send an SMS notification.
     *
     * @param request SMS request containing phone number, message, and sender ID
     * @return async response with success/failure details
     */
    @PostMapping("/sms/send")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendSms(
            @RequestBody SmsRequest request) {

        log.info("SMS request received for phone: {}", maskPhone(request.getPhoneNumber()));

        return notificationService.sendSms(
                request.getPhoneNumber(),
                request.getMessage(),
                request.getSenderId()
        ).thenApply(success -> {
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "SMS sent successfully",
                        "phoneNumber", maskPhone(request.getPhoneNumber())
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Failed to send SMS",
                        "phoneNumber", maskPhone(request.getPhoneNumber())
                ));
            }
        });
    }

    /**
     * Send an email notification.
     *
     * @param request Email request containing recipient, subject, and content
     * @return async response with success/failure details
     */
    @PostMapping("/email/send")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendEmail(
            @RequestBody EmailRequest request) {

        log.info("Email request received for: {}", request.getToEmail());

        return notificationService.sendEmail(
                request.getToEmail(),
                request.getSubject(),
                request.getHtmlContent(),
                request.getPlainContent()
        ).thenApply(success -> {
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Email sent successfully",
                        "recipient", request.getToEmail()
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Failed to send email",
                        "recipient", request.getToEmail()
                ));
            }
        });
    }

    /**
     * Send a welcome notification (typically used by user-service).
     *
     * @param request request with user details for email/SMS welcome message
     * @return async response indicating which channels were successful
     */
    @PostMapping("/welcome")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendWelcomeNotification(
            @RequestBody WelcomeNotificationRequest request) {

        log.info("Welcome notification request for user: {}", request.getUserId());

        return notificationService.sendWelcomeNotification(
                request.getUserId(),
                request.getFirstName(),
                request.getRole(),
                request.getEmail(),
                request.getPhoneNumber()
        ).thenApply(result -> ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Welcome notification sent",
                "emailSent", result.isEmailSent(),
                "smsSent", result.isSmsSent()
        )));
    }

    /**
     * Send USSD registration confirmation.
     *
     * @param request request containing phone number and synthetic email
     * @return async response with success/failure details
     */
    @PostMapping("/ussd/registration")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> sendUssdConfirmation(
            @RequestBody UssdConfirmationRequest request) {

        log.info("USSD confirmation request for phone: {}", maskPhone(request.getPhoneNumber()));

        return notificationService.sendUssdRegistrationConfirmation(
                request.getPhoneNumber(),
                request.getFirstName(),
                request.getSyntheticEmail()
        ).thenApply(success -> ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "USSD confirmation sent" : "Failed to send confirmation"
        )));
    }

    /**
     * Health check endpoint for notification-service.
     *
     * @return status of the service and individual channel checks
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        boolean smsHealthy = notificationService.checkSmsServiceHealth();
        boolean emailHealthy = notificationService.checkEmailServiceHealth();

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "notification-service",
                "checks", Map.of(
                        "sms", smsHealthy ? "UP" : "DOWN",
                        "email", emailHealthy ? "UP" : "DOWN"
                )
        ));
    }

    /**
     * Masks sensitive phone number details for logging and responses.
     * Keeps the first 3 and last 3 digits visible, hides the rest.
     *
     * @param phoneNumber raw phone number
     * @return masked phone number (e.g., 254****123)
     */
    private String maskPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 6) return "****";
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 3);
    }
}
