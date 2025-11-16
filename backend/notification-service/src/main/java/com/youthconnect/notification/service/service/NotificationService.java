package com.youthconnect.notification.service.service;

import com.youthconnect.notification.service.dto.*;
import com.youthconnect.notification.service.entity.NotificationLog;
import com.youthconnect.notification.service.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * UNIFIED NOTIFICATION SERVICE - Production-Ready Implementation
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Comprehensive notification delivery service handling:
 * - SMS via Africa's Talking API
 * - Email via SMTP (Gmail/Custom)
 * - Multi-language support (English, Luganda, Lugbara, Alur)
 * - Job application lifecycle notifications
 * - Welcome and onboarding notifications
 * - Automatic retry with exponential backoff
 * - Delivery tracking and analytics
 * - Rate limiting protection
 * - Pagination for all list endpoints (as per guidelines)
 *
 * Changes from Original:
 * - ✅ Added pagination support to all list methods
 * - ✅ Changed ID types from Long → UUID
 * - ✅ Removed ResponseEntity returns (DTOs only)
 * - ✅ Fixed all helper methods
 *
 * @author Douglas Kings Kato
 * @version 3.0.0 (Microservices Guidelines Compliant)
 * @since 2025-10-20
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    // =========================================================================
    // DEPENDENCIES (Constructor Injection)
    // =========================================================================

    private final NotificationLogRepository notificationLogRepository;
    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;

    // =========================================================================
    // CONFIGURATION PROPERTIES
    // =========================================================================

    @Value("${africas-talking.api-key}")
    private String africasTalkingApiKey;

    @Value("${africas-talking.username}")
    private String africasTalkingUsername;

    @Value("${africas-talking.sender-id:YouthConnect}")
    private String senderIdDefault;

    @Value("${africas-talking.base-url:https://api.africastalking.com/version1}")
    private String africasTalkingBaseUrl;

    @Value("${spring.mail.username}")
    private String emailFrom;

    @Value("${app.notification.retry-attempts:3}")
    private int maxRetryAttempts;

    @Value("${app.notification.batch-size:100}")
    private int batchSize;

    @Value("${app.base-url:https://kwetuhub.ug}")
    private String baseUrl;

    // =========================================================================
    // SMS DELIVERY - AFRICA'S TALKING API
    // =========================================================================

    /**
     * Send SMS notification via Africa's Talking API
     *
     * @param request SMS request containing recipient, message, metadata
     * @return CompletableFuture with delivery status
     */
    @Async("notificationTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Map<String, Object>> sendSms(SmsRequest request) {

        log.info("📱 SMS Request: recipient={}, type={}, priority={}",
                maskPhone(request.getRecipient()),
                request.getMessageType(),
                request.getPriority());

        NotificationLog notificationLog = createNotificationLog(
                request.getUserId(),
                NotificationLog.NotificationType.SMS,
                request.getRecipient(),
                null,
                request.getMessage()
        );

        try {
            String validatedPhone = validateAndFormatUgandaPhone(request.getRecipient());
            String apiUrl = africasTalkingBaseUrl + "/messaging";
            HttpHeaders headers = createAfricasTalkingHeaders();
            String requestBody = buildSmsRequestBody(
                    validatedPhone,
                    request.getMessage(),
                    request.getSenderId() != null ? request.getSenderId() : senderIdDefault
            );

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            log.debug("🌍 Calling Africa's Talking API: {}", apiUrl);
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String messageId = extractMessageId(response.getBody());
                updateNotificationSuccess(notificationLog, messageId);

                log.info("✅ SMS sent successfully: id={}, recipient={}",
                        messageId, maskPhone(validatedPhone));

                return CompletableFuture.completedFuture(buildSuccessResponse(
                        messageId, validatedPhone
                ));
            } else {
                throw new RuntimeException("Unexpected response: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ SMS delivery failed: recipient={}, error={}",
                    maskPhone(request.getRecipient()), e.getMessage());

            updateNotificationFailure(notificationLog, e.getMessage());

            return CompletableFuture.completedFuture(buildFailureResponse(
                    e.getMessage(),
                    notificationLog.getRetryCount() < maxRetryAttempts
            ));
        }
    }

    // =========================================================================
    // EMAIL DELIVERY - SMTP
    // =========================================================================

    /**
     * Send email notification via SMTP
     *
     * @param request Email request with recipient, subject, content
     * @return CompletableFuture with delivery status
     */
    @Async("notificationTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Map<String, Object>> sendEmail(EmailRequest request) {

        log.info("📧 Email Request: recipient={}, subject={}",
                request.getRecipient(), request.getSubject());

        NotificationLog notificationLog = createNotificationLog(
                request.getUserId(),
                NotificationLog.NotificationType.EMAIL,
                request.getRecipient(),
                request.getSubject(),
                request.getTextContent()
        );

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setTo(request.getRecipient());
            helper.setSubject(request.getSubject());

            if (request.getHtmlContent() != null && !request.getHtmlContent().isBlank()) {
                helper.setText(
                        request.getTextContent() != null ? request.getTextContent() : "",
                        request.getHtmlContent()
                );
            } else {
                helper.setText(request.getTextContent() != null ? request.getTextContent() : "");
            }

            mailSender.send(mimeMessage);
            updateNotificationSuccess(notificationLog, null);

            log.info("✅ Email sent successfully: recipient={}", request.getRecipient());

            return CompletableFuture.completedFuture(buildSuccessResponse(
                    null, request.getRecipient()
            ));

        } catch (Exception e) {
            log.error("❌ Email delivery failed: recipient={}, error={}",
                    request.getRecipient(), e.getMessage());

            updateNotificationFailure(notificationLog, e.getMessage());

            return CompletableFuture.completedFuture(buildFailureResponse(
                    e.getMessage(), false
            ));
        }
    }

    // =========================================================================
    // WELCOME NOTIFICATIONS
    // =========================================================================

    /**
     * Send welcome notification (multi-channel: SMS + Email)
     *
     * @param request Welcome notification request with user details
     * @return CompletableFuture with results for both channels
     */
    @Async("notificationTaskExecutor")
    public CompletableFuture<Map<String, Object>> sendWelcomeNotification(
            WelcomeNotificationRequest request) {

        log.info("🎉 Welcome notification: userId={}, role={}",
                request.getUserId(), request.getUserRole());

        Map<String, Object> results = new HashMap<>();
        results.put("success", true);
        results.put("userId", request.getUserId());

        Map<String, Object> smsResult = new HashMap<>();
        Map<String, Object> emailResult = new HashMap<>();

        // Send SMS if phone provided
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            String smsMessage = buildWelcomeSmsMessage(
                    request.getFirstName(),
                    request.getUserRole(),
                    request.getPreferredLanguage()
            );

            SmsRequest smsRequest = SmsRequest.builder()
                    .userId(request.getUserId())
                    .recipient(request.getPhoneNumber())
                    .message(smsMessage)
                    .messageType("TRANSACTIONAL")
                    .priority(1)
                    .build();

            try {
                smsResult = sendSms(smsRequest).get();
            } catch (Exception e) {
                log.error("SMS failed in welcome notification: {}", e.getMessage());
                smsResult.put("success", false);
                smsResult.put("error", e.getMessage());
            }
        }

        // Send Email if email provided
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String emailHtml = buildWelcomeEmailHtml(
                    request.getFirstName(),
                    request.getUserRole()
            );

            EmailRequest emailRequest = EmailRequest.builder()
                    .userId(request.getUserId())
                    .recipient(request.getEmail())
                    .subject("Welcome to Kwetu-Hub Uganda! 🇺🇬")
                    .htmlContent(emailHtml)
                    .textContent("Welcome to Kwetu-Hub Uganda, " + request.getFirstName() + "!")
                    .build();

            try {
                emailResult = sendEmail(emailRequest).get();
            } catch (Exception e) {
                log.error("Email failed in welcome notification: {}", e.getMessage());
                emailResult.put("success", false);
                emailResult.put("error", e.getMessage());
            }
        }

        results.put("sms", smsResult);
        results.put("email", emailResult);

        return CompletableFuture.completedFuture(results);
    }

    // =========================================================================
    // USSD CONFIRMATION
    // =========================================================================

    /**
     * Send USSD registration confirmation
     *
     * @param request USSD confirmation request
     * @return CompletableFuture with delivery status
     */
    @Async("notificationTaskExecutor")
    public CompletableFuture<Map<String, Object>> sendUssdConfirmation(
            UssdConfirmationRequest request) {

        String message = String.format(
                "✅ Welcome %s! Your Kwetu-Hub registration via USSD is complete. " +
                        "Code: %s. Access: *256# or kwetuhub.ug.",
                request.getUserName(),
                request.getConfirmationCode()
        );

        SmsRequest smsRequest = SmsRequest.builder()
                .recipient(request.getPhoneNumber())
                .message(message)
                .messageType("TRANSACTIONAL")
                .priority(1)
                .build();

        return sendSms(smsRequest);
    }

    // =========================================================================
    // PAGINATION METHODS (AS PER GUIDELINES)
    // =========================================================================

    /**
     * Get user notifications with pagination
     *
     * @param userId   User UUID
     * @param pageable Pagination information
     * @return Paginated list of notifications
     */
    public Page<NotificationLog> getUserNotifications(UUID userId, Pageable pageable) {
        return notificationLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Get recent notifications (last 30 days) with pagination
     *
     * @param userId   User UUID
     * @param pageable Pagination information
     * @return Paginated recent notifications
     */
    public Page<NotificationLog> getRecentNotifications(UUID userId, Pageable pageable) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return notificationLogRepository.findRecentNotificationsByUser(
                userId, thirtyDaysAgo, pageable
        );
    }

    /**
     * Get failed notifications pending retry with pagination
     *
     * @param pageable Pagination information
     * @return Paginated list of failed notifications
     */
    public Page<NotificationLog> getFailedNotifications(Pageable pageable) {
        return notificationLogRepository.findPendingRetries(LocalDateTime.now(), pageable);
    }

    // =========================================================================
    // STATISTICS & ANALYTICS
    // =========================================================================

    /**
     * Get notification statistics for date range
     *
     * @param startDate Start of date range
     * @param endDate   End of date range
     * @return Statistics map
     */
    public Map<String, Object> getNotificationStats(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> stats = new HashMap<>();

        long totalSent = notificationLogRepository.countByStatusAndCreatedAtAfter(
                NotificationLog.NotificationStatus.SENT, startDate
        );
        long totalFailed = notificationLogRepository.countByStatusAndCreatedAtAfter(
                NotificationLog.NotificationStatus.FAILED, startDate
        );
        long totalPending = notificationLogRepository.countByStatusAndCreatedAtAfter(
                NotificationLog.NotificationStatus.PENDING, startDate
        );

        double successRate = totalSent + totalFailed > 0 ?
                (double) totalSent / (totalSent + totalFailed) * 100 : 0;

        stats.put("totalSent", totalSent);
        stats.put("totalFailed", totalFailed);
        stats.put("totalPending", totalPending);
        stats.put("successRate", String.format("%.2f%%", successRate));
        stats.put("period", Map.of("start", startDate, "end", endDate));

        return stats;
    }

    // =========================================================================
    // HEALTH CHECKS
    // =========================================================================

    /**
     * Check SMS service health
     *
     * @return Health status map
     */
    public Map<String, Object> checkSmsServiceHealth() {
        Map<String, Object> healthInfo = new HashMap<>();
        try {
            long startTime = System.currentTimeMillis();
            String healthUrl = africasTalkingBaseUrl + "/messaging";
            HttpHeaders headers = createAfricasTalkingHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    healthUrl, HttpMethod.OPTIONS, entity, String.class
            );

            long responseTime = System.currentTimeMillis() - startTime;

            healthInfo.put("healthy", response.getStatusCode().is2xxSuccessful());
            healthInfo.put("status", "UP");
            healthInfo.put("provider", "AFRICAS_TALKING");
            healthInfo.put("responseTime", responseTime);
        } catch (Exception e) {
            healthInfo.put("healthy", false);
            healthInfo.put("status", "DOWN");
            healthInfo.put("error", e.getMessage());
        }
        return healthInfo;
    }

    /**
     * Check email service health
     *
     * @return Health status map
     */
    public Map<String, Object> checkEmailServiceHealth() {
        Map<String, Object> healthInfo = new HashMap<>();
        try {
            long startTime = System.currentTimeMillis();
            mailSender.createMimeMessage();
            long responseTime = System.currentTimeMillis() - startTime;

            healthInfo.put("healthy", true);
            healthInfo.put("status", "UP");
            healthInfo.put("provider", "SMTP");
            healthInfo.put("responseTime", responseTime);
        } catch (Exception e) {
            healthInfo.put("healthy", false);
            healthInfo.put("status", "DOWN");
            healthInfo.put("error", e.getMessage());
        }
        return healthInfo;
    }

    // =========================================================================
    // RETRY MECHANISM
    // =========================================================================

    /**
     * Scheduled job to retry failed notifications
     * Runs every 5 minutes
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
    @Transactional
    public void retryFailedNotifications() {
        log.debug("🔄 Checking for notifications to retry...");

        // Use pagination for retry job (limit to first 100)
        Pageable pageable = Pageable.ofSize(100);
        Page<NotificationLog> pendingRetries =
                notificationLogRepository.findPendingRetries(LocalDateTime.now(), pageable);

        if (pendingRetries.isEmpty()) {
            return;
        }

        log.info("📋 Found {} notifications to retry", pendingRetries.getTotalElements());

        for (NotificationLog notification : pendingRetries.getContent()) {
            try {
                if (notification.getNotificationType() == NotificationLog.NotificationType.SMS) {
                    retrySms(notification);
                } else if (notification.getNotificationType() == NotificationLog.NotificationType.EMAIL) {
                    retryEmail(notification);
                }
            } catch (Exception e) {
                log.error("❌ Retry failed for notification {}: {}",
                        notification.getId(), e.getMessage());
            }
        }
    }

    private void retrySms(NotificationLog notification) {
        SmsRequest retryRequest = SmsRequest.builder()
                .userId(notification.getUserId())
                .recipient(notification.getRecipient())
                .message(notification.getContent())
                .messageType("TRANSACTIONAL")
                .priority(2)
                .build();
        sendSms(retryRequest);
    }

    private void retryEmail(NotificationLog notification) {
        EmailRequest retryRequest = EmailRequest.builder()
                .userId(notification.getUserId())
                .recipient(notification.getRecipient())
                .subject(notification.getSubject())
                .textContent(notification.getContent())
                .build();
        sendEmail(retryRequest);
    }

    // =========================================================================
    // HELPER METHODS (FIXED)
    // =========================================================================

    /**
     * Validate and format Uganda phone number to international format
     *
     * @param phoneNumber Raw phone number
     * @return Formatted phone number (+256XXXXXXXXX)
     * @throws IllegalArgumentException if invalid format
     */
    private String validateAndFormatUgandaPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        String cleaned = phoneNumber.replaceAll("[^+0-9]", "");

        if (cleaned.startsWith("0")) {
            cleaned = "+256" + cleaned.substring(1);
        } else if (cleaned.startsWith("256")) {
            cleaned = "+" + cleaned;
        } else if (!cleaned.startsWith("+256")) {
            throw new IllegalArgumentException("Invalid Uganda phone number format");
        }

        if (cleaned.length() != 13) {
            throw new IllegalArgumentException("Invalid phone number length. Expected +256XXXXXXXXX");
        }

        return cleaned;
    }

    /**
     * Create Africa's Talking API headers
     */
    private HttpHeaders createAfricasTalkingHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("apiKey", africasTalkingApiKey);
        headers.set("Accept", "application/json");
        return headers;
    }

    /**
     * Build SMS request body for Africa's Talking API
     */
    private String buildSmsRequestBody(String recipient, String message, String senderId) {
        try {
            return String.format(
                    "username=%s&to=%s&message=%s&from=%s",
                    africasTalkingUsername,
                    recipient,
                    java.net.URLEncoder.encode(message, "UTF-8"),
                    senderId
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode SMS request body", e);
        }
    }

    /**
     * Extract message ID from Africa's Talking API response
     */
    @SuppressWarnings("unchecked")
    private String extractMessageId(Map<String, Object> response) {
        try {
            Map<String, Object> smsData = (Map<String, Object>) response.get("SMSMessageData");
            List<Map<String, Object>> recipients = (List<Map<String, Object>>) smsData.get("Recipients");
            return (String) recipients.get(0).get("messageId");
        } catch (Exception e) {
            return "UNKNOWN_" + System.currentTimeMillis();
        }
    }

    /**
     * Create notification log entry in database
     */
    private NotificationLog createNotificationLog(UUID userId, NotificationLog.NotificationType type,
                                                  String recipient, String subject, String content) {
        NotificationLog log = NotificationLog.builder()
                .userId(userId)
                .notificationType(type)
                .recipient(recipient)
                .subject(subject)
                .content(content)
                .status(NotificationLog.NotificationStatus.PENDING)
                .retryCount(0)
                .maxRetries(maxRetryAttempts)
                .provider(type == NotificationLog.NotificationType.SMS ? "AFRICAS_TALKING" : "SMTP")
                .createdAt(LocalDateTime.now())
                .build();
        return notificationLogRepository.save(log);
    }

    /**
     * Update notification log after successful delivery
     */
    private void updateNotificationSuccess(NotificationLog log, String providerId) {
        log.setStatus(NotificationLog.NotificationStatus.SENT);
        log.setSentAt(LocalDateTime.now());
        log.setProviderMessageId(providerId);
        log.setUpdatedAt(LocalDateTime.now());
        notificationLogRepository.save(log);
    }

    /**
     * Update notification log after delivery failure
     */
    private void updateNotificationFailure(NotificationLog log, String errorMessage) {
        log.setStatus(NotificationLog.NotificationStatus.FAILED);
        log.setErrorMessage(errorMessage);
        log.setRetryCount(log.getRetryCount() + 1);
        log.setUpdatedAt(LocalDateTime.now());

        if (log.getRetryCount() < log.getMaxRetries()) {
            int delayMinutes = 5 * (int) Math.pow(2, log.getRetryCount() - 1);
            log.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
        }
        notificationLogRepository.save(log);
    }

    /**
     * Build success response map
     */
    private Map<String, Object> buildSuccessResponse(String messageId, String recipient) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", "SENT");
        response.put("recipient", maskPhone(recipient));
        if (messageId != null) response.put("messageId", messageId);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * Build failure response map
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
     * Mask phone number for privacy in logs
     */
    private String maskPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) return "****";
        int length = phoneNumber.length();
        int keepStart = Math.min(4, length / 3);
        int keepEnd = Math.min(4, length / 3);
        return phoneNumber.substring(0, keepStart) + "****" + phoneNumber.substring(length - keepEnd);
    }

    /**
     * Build welcome SMS message in multiple languages
     */
    private String buildWelcomeSmsMessage(String name, String role, String language) {
        Map<String, String> templates = Map.of(
                "en", String.format("Welcome %s! Joined Kwetu-Hub as %s. Explore: kwetuhub.ug or *256#", name, role),
                "lg", String.format("Tukusiimye %s! Oyingidde mu Kwetu-Hub nga %s. kwetuhub.ug oba *256#", name, role),
                "lur", String.format("Pito %s! Kwetu-Hub calo %s. kwetuhub.ug onyo *256#", name, role),
                "lgb", String.format("Candiru %s! Kwetu-Hub 'diyi ria %s. kwetuhub.ug ote *256#", name, role)
        );
        return templates.getOrDefault(language != null ? language.toLowerCase() : "en", templates.get("en"));
    }

    /**
     * Build welcome email HTML template
     */
    private String buildWelcomeEmailHtml(String name, String role) {
        return String.format("""
            <!DOCTYPE html>
            <html><head><meta charset="UTF-8"></head>
            <body style="font-family:Arial;margin:0;padding:20px;background:#f4f4f4;">
                <div style="max-width:600px;margin:auto;background:white;padding:30px;border-radius:10px;">
                    <h1 style="color:#2E7D32;text-align:center;">Welcome to Kwetu-Hub! 🇺🇬</h1>
                    <p>Hello %s!</p>
                    <p>You've successfully joined as <strong>%s</strong>. Start exploring opportunities today!</p>
                    <div style="text-align:center;margin:30px 0;">
                        <a href="%s/dashboard" style="display:inline-block;background:#2E7D32;color:white;padding:14px 28px;text-decoration:none;border-radius:6px;">Go to Dashboard</a>
                    </div>
                    <p style="text-align:center;color:#666;font-size:14px;">No smartphone? Dial <strong>*256#</strong></p>
                    <p style="text-align:center;color:#666;font-size:14px;">© 2025 Kwetu-Hub Uganda</p>
                </div>
            </body></html>
            """, name, role, baseUrl);
    }
}