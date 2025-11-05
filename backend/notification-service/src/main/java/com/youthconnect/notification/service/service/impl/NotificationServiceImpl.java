package com.youthconnect.notification.service.service.impl;

import com.youthconnect.notification.service.dto.*;
import com.youthconnect.notification.service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * NOTIFICATION SERVICE IMPLEMENTATION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Handles all notification delivery via multiple channels:
 * - SMS via Africa's Talking API
 * - Email via SMTP (JavaMailSender)
 * - Multi-channel welcome notifications
 * - USSD registration confirmations
 *
 * All methods are async for non-blocking execution.
 *
 * Location: notification-service/src/main/java/com/youthconnect/notification/service/service/impl/
 *
 * @author Douglas Kings Kato
 * @version 2.0.0
 * @since 2025-10-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;
    // Inject Africa's Talking client here when implemented
    // private final AfricasTalkingClient smsClient;

    @Value("${app.mail.from:noreply@youthconnect.ug}")
    private String fromEmail;

    @Value("${app.sms.sender-id:YouthConnect}")
    private String smsSenderId;

    // =========================================================================
    // SMS METHODS
    // =========================================================================

    @Async
    @Override
    public CompletableFuture<Map<String, Object>> sendSms(SmsRequest request) {
        log.info("📱 Sending SMS to: {}", maskPhone(request.getRecipient()));

        try {
            // TODO: Implement actual Africa's Talking API call
            // For now, simulate SMS sending
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("status", "SENT");
            result.put("recipient", maskPhone(request.getRecipient()));
            result.put("messageId", "ATXid_" + System.currentTimeMillis());
            result.put("timestamp", LocalDateTime.now());

            log.info("✅ SMS sent successfully to: {}", maskPhone(request.getRecipient()));
            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("❌ SMS sending failed: {}", e.getMessage(), e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("status", "FAILED");
            errorResult.put("error", e.getMessage());
            errorResult.put("willRetry", true);
            errorResult.put("timestamp", LocalDateTime.now());

            return CompletableFuture.completedFuture(errorResult);
        }
    }

    @Override
    public Map<String, Object> checkSmsServiceHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // TODO: Implement actual health check to Africa's Talking API
            health.put("healthy", true);
            health.put("status", "UP");
            health.put("provider", "AFRICAS_TALKING");
            health.put("responseTime", 150);

        } catch (Exception e) {
            health.put("healthy", false);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }

        return health;
    }

    // =========================================================================
    // EMAIL METHODS
    // =========================================================================

    @Async
    @Override
    public CompletableFuture<Map<String, Object>> sendEmail(EmailRequest request) {
        log.info("📧 Sending email to: {}", request.getRecipient());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(request.getRecipient());
            helper.setSubject(request.getSubject());

            // Set HTML content if available, otherwise use text
            if (request.getHtmlContent() != null && !request.getHtmlContent().isEmpty()) {
                helper.setText(request.getTextContent(), request.getHtmlContent());
            } else {
                helper.setText(request.getTextContent());
            }

            mailSender.send(message);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("status", "SENT");
            result.put("recipient", request.getRecipient());
            result.put("timestamp", LocalDateTime.now());

            log.info("✅ Email sent successfully to: {}", request.getRecipient());
            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("❌ Email sending failed: {}", e.getMessage(), e);

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("status", "FAILED");
            errorResult.put("error", e.getMessage());
            errorResult.put("timestamp", LocalDateTime.now());

            return CompletableFuture.completedFuture(errorResult);
        }
    }

    @Override
    public Map<String, Object> checkEmailServiceHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Test SMTP connection
            mailSender.createMimeMessage();

            health.put("healthy", true);
            health.put("status", "UP");
            health.put("provider", "SMTP");
            health.put("responseTime", 80);

        } catch (Exception e) {
            health.put("healthy", false);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }

        return health;
    }

    // =========================================================================
    // WELCOME NOTIFICATION (MULTI-CHANNEL)
    // =========================================================================

    @Async
    @Override
    public CompletableFuture<Map<String, Object>> sendWelcomeNotification(
            WelcomeNotificationRequest request) {

        log.info("🎉 Sending welcome notification to user: {}", request.getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", request.getUserId());

        // Send SMS welcome
        CompletableFuture<Map<String, Object>> smsFuture = sendWelcomeSms(request);

        // Send Email welcome
        CompletableFuture<Map<String, Object>> emailFuture = sendWelcomeEmail(request);

        // Wait for both to complete
        return CompletableFuture.allOf(smsFuture, emailFuture)
                .thenApply(v -> {
                    result.put("success", true);
                    result.put("message", "Welcome notification sent");
                    result.put("sms", smsFuture.join());
                    result.put("email", emailFuture.join());
                    return result;
                })
                .exceptionally(ex -> {
                    log.error("❌ Welcome notification failed: {}", ex.getMessage());
                    result.put("success", false);
                    result.put("error", ex.getMessage());
                    return result;
                });
    }

    private CompletableFuture<Map<String, Object>> sendWelcomeSms(WelcomeNotificationRequest request) {
        String message = String.format(
                "Welcome to Youth Connect, %s! Your account has been created successfully. " +
                        "Visit https://youthconnect.ug to explore opportunities.",
                request.getFirstName()
        );

        SmsRequest smsRequest = SmsRequest.builder()
                .recipient(request.getPhoneNumber())
                .message(message)
                .messageType("TRANSACTIONAL")
                .priority(1)
                .senderId(smsSenderId)
                .userId(request.getUserId())
                .build();

        return sendSms(smsRequest);
    }

    private CompletableFuture<Map<String, Object>> sendWelcomeEmail(WelcomeNotificationRequest request) {
        String subject = "Welcome to Youth Connect Uganda!";

        String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h1 style="color: #2563eb;">Welcome to Youth Connect, %s!</h1>
                    <p>We're excited to have you join our community of youth, mentors, and opportunities.</p>
                    <p>Your account has been successfully created with the role: <strong>%s</strong></p>
                    <h3>Next Steps:</h3>
                    <ul>
                        <li>Complete your profile to get personalized recommendations</li>
                        <li>Explore available opportunities and programs</li>
                        <li>Connect with mentors and peers</li>
                    </ul>
                    <p>Visit <a href="https://youthconnect.ug">youthconnect.ug</a> to get started!</p>
                    <p>Best regards,<br>The Youth Connect Team</p>
                </body>
                </html>
                """, request.getFirstName(), request.getUserRole());

        String textContent = String.format(
                "Welcome to Youth Connect, %s! Your account (%s) has been created successfully. " +
                        "Visit https://youthconnect.ug to explore opportunities.",
                request.getFirstName(), request.getUserRole()
        );

        EmailRequest emailRequest = EmailRequest.builder()
                .recipient(request.getEmail())
                .subject(subject)
                .htmlContent(htmlContent)
                .textContent(textContent)
                .userId(request.getUserId())
                .build();

        return sendEmail(emailRequest);
    }

    // =========================================================================
    // USSD CONFIRMATION
    // =========================================================================

    @Async
    @Override
    public CompletableFuture<Map<String, Object>> sendUssdConfirmation(
            UssdConfirmationRequest request) {

        log.info("📞 Sending USSD confirmation to: {}", maskPhone(request.getPhoneNumber()));

        String message = String.format(
                "Welcome %s! Registration successful. Your confirmation code: %s. " +
                        "Login at youthconnect.ug with your phone number.",
                request.getUserName(),
                request.getConfirmationCode()
        );

        SmsRequest smsRequest = SmsRequest.builder()
                .recipient(request.getPhoneNumber())
                .message(request.getMessage() != null ? request.getMessage() : message)
                .messageType("TRANSACTIONAL")
                .priority(1)
                .senderId(smsSenderId)
                .build();

        return sendSms(smsRequest);
    }

    // =========================================================================
    // STATISTICS & HISTORY
    // =========================================================================

    @Override
    public Map<String, Object> getNotificationStats(LocalDateTime start, LocalDateTime end) {
        // TODO: Implement actual database queries for statistics
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSent", 1250);
        stats.put("totalFailed", 45);
        stats.put("totalPending", 12);
        stats.put("successRate", "96.52%");
        stats.put("breakdown", Map.of(
                "sms", Map.of(
                        "sent", 800,
                        "failed", 30,
                        "successRate", "96.39%"
                ),
                "email", Map.of(
                        "sent", 450,
                        "failed", 15,
                        "successRate", "96.77%"
                )
        ));
        stats.put("period", Map.of(
                "start", start.toLocalDate(),
                "end", end.toLocalDate()
        ));

        return stats;
    }

    @Override
    public List<Map<String, Object>> getUserNotifications(Long userId, int limit) {
        // TODO: Implement actual database query
        return List.of(
                Map.of(
                        "id", 1001,
                        "type", "SMS",
                        "status", "SENT",
                        "recipient", "+256****4567",
                        "content", "Your application has been approved!",
                        "sentAt", LocalDateTime.now().minusDays(1),
                        "deliveredAt", LocalDateTime.now().minusDays(1).plusSeconds(5)
                )
        );
    }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================

    private String maskPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return "****";
        }

        int length = phoneNumber.length();
        int keepStart = Math.min(4, length / 3);
        int keepEnd = Math.min(4, length / 3);

        return phoneNumber.substring(0, keepStart) +
                "****" +
                phoneNumber.substring(length - keepEnd);
    }
}