package com.youthconnect.notification.service.service;

import com.youthconnect.notification_service.dto.*;
import com.youthconnect.notification_service.entity.NotificationLog;
import com.youthconnect.notification_service.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Core notification service handling SMS, Email, and Push notifications
 * Integrates with Africa's Talking for SMS delivery
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${africas-talking.api-key:DEMO_API_KEY}")
    private String africasTalkingApiKey;

    @Value("${africas-talking.username:sandbox}")
    private String africasTalkingUsername;

    @Value("${africas-talking.sender-id:YouthConnect}")
    private String senderIdDefault;

    @Value("${spring.mail.username:noreply@youthconnect.ug}")
    private String emailFrom;

    // =========================================================================
    // SMS SENDING
    // =========================================================================

    /**
     * Send SMS via Africa's Talking API
     * @param request SMS request details
     * @return CompletableFuture with result
     */
    @Async
    @Transactional
    public CompletableFuture<Map<String, Object>> sendSms(SmsRequest request) {
        log.info("Sending SMS to: {}", request.getRecipient());

        // Create notification log
        NotificationLog notificationLog = NotificationLog.builder()
                .userId(request.getUserId())
                .notificationType(NotificationLog.NotificationType.SMS)
                .recipient(request.getRecipient())
                .content(request.getMessage())
                .status(NotificationLog.NotificationStatus.PENDING)
                .provider("AFRICAS_TALKING")
                .build();

        notificationLogRepository.save(notificationLog);

        try {
            // Africa's Talking API endpoint
            String apiUrl = "https://api.africastalking.com/version1/messaging";

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("apiKey", africasTalkingApiKey);
            headers.set("Accept", "application/json");

            // Prepare request body
            String senderId = request.getSenderId() != null ?
                    request.getSenderId() : senderIdDefault;

            String requestBody = String.format(
                    "username=%s&to=%s&message=%s&from=%s",
                    africasTalkingUsername,
                    request.getRecipient(),
                    java.net.URLEncoder.encode(request.getMessage(), "UTF-8"),
                    senderId
            );

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            // Send request
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl, HttpMethod.POST, entity, Map.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED ||
                    response.getStatusCode() == HttpStatus.OK) {

                // Extract message ID from response
                Map<String, Object> responseBody = response.getBody();
                String messageId = extractMessageId(responseBody);

                notificationLog.setStatus(NotificationLog.NotificationStatus.SENT);
                notificationLog.setSentAt(LocalDateTime.now());
                notificationLog.setProviderMessageId(messageId);
                notificationLogRepository.save(notificationLog);

                log.info("SMS sent successfully to: {}", request.getRecipient());

                return CompletableFuture.completedFuture(Map.of(
                        "success", true,
                        "messageId", messageId,
                        "recipient", request.getRecipient()
                ));
            } else {
                throw new RuntimeException("Unexpected response: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", request.getRecipient(), e.getMessage());

            notificationLog.setStatus(NotificationLog.NotificationStatus.FAILED);
            notificationLog.setErrorMessage(e.getMessage());
            notificationLog.setRetryCount(notificationLog.getRetryCount() + 1);

            // Schedule retry if within retry limit
            if (notificationLog.getRetryCount() < notificationLog.getMaxRetries()) {
                notificationLog.setNextRetryAt(
                        LocalDateTime.now().plusMinutes(5 * notificationLog.getRetryCount())
                );
            }

            notificationLogRepository.save(notificationLog);

            return CompletableFuture.completedFuture(Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "willRetry", notificationLog.getRetryCount() < notificationLog.getMaxRetries()
            ));
        }
    }

    private String extractMessageId(Map<String, Object> response) {
        try {
            Map<String, Object> smsResponse =
                    (Map<String, Object>) response.get("SMSMessageData");
            List<Map<String, Object>> recipients =
                    (List<Map<String, Object>>) smsResponse.get("Recipients");
            return (String) recipients.get(0).get("messageId");
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    // =========================================================================
    // EMAIL SENDING
    // =========================================================================

    /**
     * Send email notification
     */
    @Async
    @Transactional
    public CompletableFuture<Map<String, Object>> sendEmail(EmailRequest request) {
        log.info("Sending email to: {}", request.getRecipient());

        NotificationLog notificationLog = NotificationLog.builder()
                .userId(request.getUserId())
                .notificationType(NotificationLog.NotificationType.EMAIL)
                .recipient(request.getRecipient())
                .subject(request.getSubject())
                .content(request.getHtmlContent() != null ?
                        request.getHtmlContent() : request.getTextContent())
                .status(NotificationLog.NotificationStatus.PENDING)
                .provider("SMTP")
                .build();

        notificationLogRepository.save(notificationLog);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(emailFrom);
            helper.setTo(request.getRecipient());
            helper.setSubject(request.getSubject());

            if (request.getHtmlContent() != null) {
                helper.setText(request.getTextContent(), request.getHtmlContent());
            } else {
                helper.setText(request.getTextContent());
            }

            mailSender.send(mimeMessage);

            notificationLog.setStatus(NotificationLog.NotificationStatus.SENT);
            notificationLog.setSentAt(LocalDateTime.now());
            notificationLogRepository.save(notificationLog);

            log.info("Email sent successfully to: {}", request.getRecipient());

            return CompletableFuture.completedFuture(Map.of(
                    "success", true,
                    "recipient", request.getRecipient()
            ));

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", request.getRecipient(), e.getMessage());

            notificationLog.setStatus(NotificationLog.NotificationStatus.FAILED);
            notificationLog.setErrorMessage(e.getMessage());
            notificationLogRepository.save(notificationLog);

            return CompletableFuture.completedFuture(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // =========================================================================
    // WELCOME NOTIFICATION
    // =========================================================================

    /**
     * Send welcome notification (SMS + Email)
     */
    @Async
    @Transactional
    public CompletableFuture<Map<String, Object>> sendWelcomeNotification(
            WelcomeNotificationRequest request) {

        log.info("Sending welcome notification for user: {}", request.getUserId());

        Map<String, Object> results = new HashMap<>();

        // Send welcome SMS
        if (request.getPhoneNumber() != null) {
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

            CompletableFuture<Map<String, Object>> smsResult = sendSms(smsRequest);
            results.put("sms", smsResult.join());
        }

        // Send welcome email
        if (request.getEmail() != null) {
            String emailHtml = buildWelcomeEmailHtml(
                    request.getFirstName(),
                    request.getUserRole()
            );

            EmailRequest emailRequest = EmailRequest.builder()
                    .userId(request.getUserId())
                    .recipient(request.getEmail())
                    .subject("Welcome to Kwetu-Hub Uganda!")
                    .htmlContent(emailHtml)
                    .textContent("Welcome to Kwetu-Hub Uganda, " + request.getFirstName() + "!")
                    .build();

            CompletableFuture<Map<String, Object>> emailResult = sendEmail(emailRequest);
            results.put("email", emailResult.join());
        }

        results.put("success", true);
        return CompletableFuture.completedFuture(results);
    }

    // =========================================================================
    // USSD CONFIRMATION
    // =========================================================================

    @Async
    public CompletableFuture<Map<String, Object>> sendUssdConfirmation(
            UssdConfirmationRequest request) {

        String message = String.format(
                "Welcome %s! Your registration is successful. " +
                        "Confirmation code: %s. Access Kwetu-Hub via *256# or web.",
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
    // RETRY MECHANISM (Scheduled Task)
    // =========================================================================

    /**
     * Retry failed notifications every 5 minutes
     */
    @Scheduled(fixedDelay = 300000) // 5 minutes
    @Transactional
    public void retryFailedNotifications() {
        log.debug("Checking for notifications to retry...");

        List<NotificationLog> pendingRetries =
                notificationLogRepository.findPendingRetries(LocalDateTime.now());

        log.info("Found {} notifications to retry", pendingRetries.size());

        for (NotificationLog notification : pendingRetries) {
            try {
                if (notification.getNotificationType() == NotificationLog.NotificationType.SMS) {
                    SmsRequest retryRequest = SmsRequest.builder()
                            .userId(notification.getUserId())
                            .recipient(notification.getRecipient())
                            .message(notification.getContent())
                            .messageType("TRANSACTIONAL")
                            .priority(2)
                            .build();

                    sendSms(retryRequest);
                }
                // Add email retry logic similarly

            } catch (Exception e) {
                log.error("Retry failed for notification {}: {}",
                        notification.getId(), e.getMessage());
            }
        }
    }

    // =========================================================================
    // MESSAGE TEMPLATES
    // =========================================================================

    private String buildWelcomeSmsMessage(String name, String role, String language) {
        // Multilingual support
        Map<String, String> templates = Map.of(
                "en", "Welcome %s! You've joined Kwetu-Hub as %s. " +
                        "Dial *256# or visit kwetuhub.ug to explore opportunities.",
                "lg", "Tukusiimye %s! Oyingidde mu YouthConnect nga %s. " +
                        "Yingiza *256# oba lambula youthconnect.ug.",
                "lur", "Pito %s! In donyo YouthConnect calo %s. " +
                        "Col *256# onyo yab kwetuhub.ug.",
                "lgb", "Candiru %s! E yo Kwetu-Hub 'diyi ria %s. " +
                        "Drial *256# ote web kwetuhub.ug."
        );

        String template = templates.getOrDefault(language, templates.get("en"));
        return String.format(template, name, role);
    }

    private String buildWelcomeEmailHtml(String name, String role) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #2E7D32; color: white; padding: 20px; text-align: center; }
                    .content { padding: 30px; background: #f9f9f9; }
                    .button { 
                        background: #2E7D32; 
                        color: white; 
                        padding: 12px 30px; 
                        text-decoration: none; 
                        border-radius: 5px;
                        display: inline-block;
                        margin: 20px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to Kwetu-Hub Uganda!</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Thank you for joining Kwetu-Hub Uganda as a <strong>%s</strong>!</p>
                        <p>You now have access to:</p>
                        <ul>
                            <li>Job opportunities and training programs</li>
                            <li>Business grants and loans</li>
                            <li>Mentorship from experienced professionals</li>
                            <li>Community support and networking</li>
                            <li>Skills development resources</li>
                        </ul>
                        <p>Get started by exploring opportunities on our platform:</p>
                        <a href="https://kwetuhub.ug" class="button">Visit Dashboard</a>
                        <p>You can also access our platform via USSD by dialing <strong>*256#</strong> 
                           from any phone.</p>
                        <p>Best regards,<br>The Youth Connect Uganda Team</p>
                    </div>
                </div>
            </body>
            </html>
            """, name, role);
    }
}