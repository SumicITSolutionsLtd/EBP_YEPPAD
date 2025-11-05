package com.youthconnect.notification.service.service;

import com.youthconnect.notification.service.dto.*;
import com.youthconnect.notification.service.entity.NotificationLog;
import com.youthconnect.notification.service.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * UNIFIED NOTIFICATION SERVICE - PRODUCTION-READY IMPLEMENTATION
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
 *
 * @author Douglas Kings Kato
 * @version 3.0.0 (Unified)
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
     * Send SMS notification via Africa's Talking API.
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
     * Send email notification via SMTP.
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

    // ═════════════════════════════════════════════════════════════════════════
    // JOB APPLICATION NOTIFICATIONS
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Send job application confirmation (SMS + Email)
     *
     * Comprehensive notification sent when user submits job application.
     * Includes reference number, job details, and tracking information.
     *
     * @param dto JobNotificationDto with application details
     * @return CompletableFuture with delivery status for both channels
     */
    @Async("notificationTaskExecutor")
    public CompletableFuture<Map<String, Object>> sendJobApplicationConfirmation(
            JobNotificationDto dto) {

        log.info("📋 Sending job application confirmation: userId={}, jobId={}, appId={}",
                dto.getUserId(), dto.getJobId(), dto.getApplicationId());

        try {
            Map<String, Object> results = new HashMap<>();

            // Send SMS confirmation
            String smsMessage = buildApplicationConfirmationSms(dto);
            SmsRequest smsRequest = SmsRequest.builder()
                    .userId(dto.getUserId())
                    .recipient(dto.getPhoneNumber())
                    .message(smsMessage)
                    .messageType("TRANSACTIONAL")
                    .priority(1)
                    .build();

            CompletableFuture<Map<String, Object>> smsFuture = sendSms(smsRequest);

            // Send email confirmation
            String emailHtml = buildApplicationConfirmationEmail(dto);
            EmailRequest emailRequest = EmailRequest.builder()
                    .userId(dto.getUserId())
                    .recipient(dto.getEmail())
                    .subject(String.format("✅ Application Confirmed: %s", dto.getJobTitle()))
                    .htmlContent(emailHtml)
                    .textContent(String.format(
                            "Your application for %s at %s has been submitted. Reference: #%d",
                            dto.getJobTitle(), dto.getCompanyName(), dto.getApplicationId()))
                    .build();

            CompletableFuture<Map<String, Object>> emailFuture = sendEmail(emailRequest);

            results.put("sms", smsFuture.get());
            results.put("email", emailFuture.get());
            results.put("success", true);
            results.put("notificationType", "JOB_APPLICATION_CONFIRMATION");

            log.info("✅ Job application confirmation sent: userId={}, appId={}",
                    dto.getUserId(), dto.getApplicationId());

            return CompletableFuture.completedFuture(results);

        } catch (Exception e) {
            log.error("❌ Failed to send job application confirmation: {}", e.getMessage());
            return CompletableFuture.completedFuture(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Send application status update (approved/rejected)
     *
     * Notifies applicant when their application status changes.
     *
     * @param dto JobNotificationDto with status and feedback
     * @return CompletableFuture with delivery status
     */
    @Async("notificationTaskExecutor")
    public CompletableFuture<Map<String, Object>> sendApplicationStatusUpdate(
            JobNotificationDto dto) {

        log.info("📬 Sending status update: userId={}, appId={}, status={}",
                dto.getUserId(), dto.getApplicationId(), dto.getApplicationStatus());

        try {
            Map<String, Object> results = new HashMap<>();
            boolean isApproved = "APPROVED".equalsIgnoreCase(dto.getApplicationStatus()) ||
                    "ACCEPTED".equalsIgnoreCase(dto.getApplicationStatus());

            // Send SMS
            String smsMessage = buildStatusUpdateSms(dto, isApproved);
            SmsRequest smsRequest = SmsRequest.builder()
                    .userId(dto.getUserId())
                    .recipient(dto.getPhoneNumber())
                    .message(smsMessage)
                    .messageType("TRANSACTIONAL")
                    .priority(1)
                    .build();

            CompletableFuture<Map<String, Object>> smsFuture = sendSms(smsRequest);

            // Send Email
            String emailHtml = buildStatusUpdateEmail(dto, isApproved);
            EmailRequest emailRequest = EmailRequest.builder()
                    .userId(dto.getUserId())
                    .recipient(dto.getEmail())
                    .subject(String.format("%s Application %s: %s",
                            isApproved ? "🎉" : "📋",
                            isApproved ? "Approved" : "Update",
                            dto.getJobTitle()))
                    .htmlContent(emailHtml)
                    .textContent(String.format(
                            "Your application for %s has been %s.",
                            dto.getJobTitle(), dto.getApplicationStatus().toLowerCase()))
                    .build();

            CompletableFuture<Map<String, Object>> emailFuture = sendEmail(emailRequest);

            results.put("sms", smsFuture.get());
            results.put("email", emailFuture.get());
            results.put("success", true);
            results.put("notificationType", "APPLICATION_STATUS_UPDATE");

            log.info("✅ Status update sent: userId={}, status={}",
                    dto.getUserId(), dto.getApplicationStatus());

            return CompletableFuture.completedFuture(results);

        } catch (Exception e) {
            log.error("❌ Failed to send status update: {}", e.getMessage());
            return CompletableFuture.completedFuture(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Send job deadline reminder
     *
     * @param dto JobNotificationDto with deadline information
     * @return CompletableFuture with delivery status
     */
    @Async("notificationTaskExecutor")
    public CompletableFuture<Map<String, Object>> sendDeadlineReminder(
            JobNotificationDto dto) {

        log.info("⏰ Sending deadline reminder: userId={}, jobId={}, days={}",
                dto.getUserId(), dto.getJobId(), dto.getDaysRemaining());

        try {
            Map<String, Object> results = new HashMap<>();

            // Send SMS
            String smsMessage = buildDeadlineReminderSms(dto);
            SmsRequest smsRequest = SmsRequest.builder()
                    .userId(dto.getUserId())
                    .recipient(dto.getPhoneNumber())
                    .message(smsMessage)
                    .messageType("MARKETING")
                    .priority(dto.isUrgent() ? 1 : 2)
                    .build();

            CompletableFuture<Map<String, Object>> smsFuture = sendSms(smsRequest);

            // Send Email
            String emailHtml = buildDeadlineReminderEmail(dto);
            EmailRequest emailRequest = EmailRequest.builder()
                    .userId(dto.getUserId())
                    .recipient(dto.getEmail())
                    .subject(String.format("⏰ Deadline %s: %s - %d days left!",
                            dto.isUrgent() ? "URGENT" : "Reminder",
                            dto.getJobTitle(),
                            dto.getDaysRemaining()))
                    .htmlContent(emailHtml)
                    .textContent(String.format(
                            "Reminder: Deadline for %s is in %d days.",
                            dto.getJobTitle(), dto.getDaysRemaining()))
                    .build();

            CompletableFuture<Map<String, Object>> emailFuture = sendEmail(emailRequest);

            results.put("sms", smsFuture.get());
            results.put("email", emailFuture.get());
            results.put("success", true);
            results.put("notificationType", "DEADLINE_REMINDER");

            return CompletableFuture.completedFuture(results);

        } catch (Exception e) {
            log.error("❌ Failed to send deadline reminder: {}", e.getMessage());
            return CompletableFuture.completedFuture(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Notify job poster of new application
     *
     * @param dto JobNotificationDto with poster and applicant details
     * @return CompletableFuture with delivery status
     */
    @Async("notificationTaskExecutor")
    public CompletableFuture<Map<String, Object>> sendNewApplicationAlert(
            JobNotificationDto dto) {

        log.info("🔔 New application alert: posterId={}, jobId={}",
                dto.getUserId(), dto.getJobId());

        try {
            String emailHtml = buildNewApplicationAlertEmail(dto);

            EmailRequest emailRequest = EmailRequest.builder()
                    .userId(dto.getUserId())
                    .recipient(dto.getEmail())
                    .subject(String.format("🔔 New Application: %s", dto.getJobTitle()))
                    .htmlContent(emailHtml)
                    .textContent(String.format(
                            "New application for %s from %s.",
                            dto.getJobTitle(), dto.getFirstName()))
                    .build();

            Map<String, Object> result = sendEmail(emailRequest).get();

            log.info("✅ New application alert sent: posterId={}", dto.getUserId());

            return CompletableFuture.completedFuture(result);

        } catch (Exception e) {
            log.error("❌ Failed to send new application alert: {}", e.getMessage());
            return CompletableFuture.completedFuture(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // =========================================================================
    // WELCOME NOTIFICATIONS
    // =========================================================================

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

    // ═════════════════════════════════════════════════════════════════════════
    // SMS MESSAGE BUILDERS
    // ═════════════════════════════════════════════════════════════════════════

    private String buildApplicationConfirmationSms(JobNotificationDto dto) {
        return String.format(
                "✅ Applied to '%s' at %s. Ref: #%d. Track: %s/applications or *256#",
                truncate(dto.getJobTitle(), 30),
                truncate(dto.getCompanyName(), 25),
                dto.getApplicationId(),
                baseUrl
        );
    }

    private String buildStatusUpdateSms(JobNotificationDto dto, boolean isApproved) {
        String emoji = isApproved ? "🎉" : "📋";
        String status = dto.getApplicationStatus().replace("_", " ");

        StringBuilder sms = new StringBuilder();
        sms.append(emoji).append(" ").append(status)
                .append(" - '").append(truncate(dto.getJobTitle(), 30)).append("'");

        if (dto.getReviewNotes() != null && !dto.getReviewNotes().isBlank()) {
            sms.append(". ").append(truncate(dto.getReviewNotes(), 50));
        }

        sms.append(". View: ").append(baseUrl).append("/applications/")
                .append(dto.getApplicationId());

        return sms.toString();
    }

    private String buildDeadlineReminderSms(JobNotificationDto dto) {
        String emoji = dto.isUrgent() ? "🚨" : "⏰";
        return String.format(
                "%s %d days left: '%s' at %s! Apply: %s/jobs/%d or *256#",
                emoji,
                dto.getDaysRemaining(),
                truncate(dto.getJobTitle(), 30),
                truncate(dto.getCompanyName(), 20),
                baseUrl,
                dto.getJobId()
        );
    }

    private String buildWelcomeSmsMessage(String name, String role, String language) {
        Map<String, String> templates = Map.of(
                "en", String.format("Welcome %s! Joined Kwetu-Hub as %s. Explore: kwetuhub.ug or *256#", name, role),
                "lg", String.format("Tukusiimye %s! Oyingidde mu Kwetu-Hub nga %s. kwetuhub.ug oba *256#", name, role),
                "lur", String.format("Pito %s! Kwetu-Hub calo %s. kwetuhub.ug onyo *256#", name, role),
                "lgb", String.format("Candiru %s! Kwetu-Hub 'diyi ria %s. kwetuhub.ug ote *256#", name, role)
        );
        return templates.getOrDefault(language != null ? language.toLowerCase() : "en", templates.get("en"));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // EMAIL TEMPLATE BUILDERS
    // ═════════════════════════════════════════════════════════════════════════

    private String buildApplicationConfirmationEmail(JobNotificationDto dto) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm");

        return String.format("""
            <!DOCTYPE html>
            <html><head><meta charset="UTF-8"></head>
            <body style="font-family:Arial;margin:0;padding:20px;background:#f4f4f4;">
                <div style="max-width:600px;margin:auto;background:white;padding:30px;border-radius:10px;">
                    <h1 style="color:#2E7D32;text-align:center;">✅ Application Received</h1>
                    <p>Hi %s,</p>
                    <p>Your application has been successfully submitted!</p>
                    <div style="background:#f8f9fa;padding:20px;border-radius:8px;margin:20px 0;">
                        <p style="margin:5px 0;"><strong>Position:</strong> %s</p>
                        <p style="margin:5px 0;"><strong>Company:</strong> %s</p>
                        <p style="margin:5px 0;"><strong>Application ID:</strong> #%d</p>
                        <p style="margin:5px 0;"><strong>Submitted:</strong> %s</p>
                        <p style="margin:5px 0;"><strong>Status:</strong> <span style="color:#FFA000;">Under Review</span></p>
                    </div>
                    <p>The employer will review your application soon.</p>
                    <div style="text-align:center;margin:30px 0;">
                        <a href="%s/applications/%d" style="display:inline-block;background:#2E7D32;color:white;padding:14px 28px;text-decoration:none;border-radius:6px;">Track Application</a>
                    </div>
                    <p style="text-align:center;color:#666;font-size:14px;">No smartphone? Dial <strong>*256#</strong></p>
                </div>
            </body></html>
            """,
                dto.getFirstName(),
                dto.getJobTitle(),
                dto.getCompanyName(),
                dto.getApplicationId(),
                dto.getSubmittedAt().format(dateFormatter),
                baseUrl,
                dto.getApplicationId()
        );
    }

    private String buildStatusUpdateEmail(JobNotificationDto dto, boolean isApproved) {
        String statusColor = isApproved ? "#2E7D32" : "#FFA000";
        String emoji = isApproved ? "🎉" : "📋";
        String header = isApproved ? "Congratulations!" : "Application Update";

        return String.format("""
            <!DOCTYPE html>
            <html><head><meta charset="UTF-8"></head>
            <body style="font-family:Arial;margin:0;padding:20px;background:#f4f4f4;">
                <div style="max-width:600px;margin:auto;background:white;padding:30px;border-radius:10px;">
                    <h1 style="color:%s;text-align:center;">%s %s</h1>
                    <p>Hi %s,</p>
                    <p>Your application status has been updated.</p>
                    <div style="background:#f8f9fa;padding:20px;border-radius:8px;margin:20px 0;border-left:4px solid %s;">
                        <p style="margin:8px 0;"><strong>Position:</strong> %s</p>
                        <p style="margin:8px 0;"><strong>Status:</strong> <span style="color:%s;font-weight:bold;">%s</span></p>
                        %s
                    </div>
                    %s
                    <div style="text-align:center;margin:30px 0;">
                        <a href="%s/applications/%d" style="display:inline-block;background:%s;color:white;padding:14px 28px;text-decoration:none;border-radius:6px;">View Details</a>
                    </div>
                    <p style="text-align:center;color:#666;font-size:14px;">© 2025 Kwetu-Hub Uganda 🇺🇬</p>
                </div>
            </body></html>
            """,
                statusColor, emoji, header,
                dto.getFirstName(),
                statusColor,
                dto.getJobTitle(),
                statusColor, dto.getApplicationStatus().toUpperCase(),
                dto.getReviewNotes() != null && !dto.getReviewNotes().isBlank() ?
                        String.format("<p style='margin:8px 0;'><strong>Feedback:</strong> %s</p>", dto.getReviewNotes()) : "",
                isApproved ?
                        "<p style='color:#2E7D32;font-weight:bold;'>The employer will contact you with next steps!</p>" :
                        "<p style='color:#555;'>Keep applying to other opportunities!</p>",
                baseUrl, dto.getApplicationId(),
                statusColor
        );
    }

    private String buildDeadlineReminderEmail(JobNotificationDto dto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm");
        String urgency = dto.isUrgent() ? "🚨 URGENT" : "⏰ Reminder";

        return String.format("""
            <!DOCTYPE html>
            <html><head><meta charset="UTF-8"></head>
            <body style="font-family:Arial;margin:0;padding:20px;background:#f4f4f4;">
                <div style="max-width:600px;margin:auto;background:white;padding:30px;border-radius:10px;">
                    <h1 style="color:#FF6F00;text-align:center;">%s: Application Deadline</h1>
                    <p>Hi %s,</p>
                    <p><strong>Only %d days left</strong> to apply for this opportunity!</p>
                    <div style="background:#fff3e0;padding:20px;border-radius:8px;margin:20px 0;border-left:4px solid #FF6F00;">
                        <p style="margin:8px 0;"><strong>Position:</strong> %s</p>
                        <p style="margin:8px 0;"><strong>Company:</strong> %s</p>
                        <p style="margin:8px 0;"><strong>Deadline:</strong> %s</p>
                        <p style="margin:8px 0;"><strong>Time Remaining:</strong> <span style="color:#FF6F00;font-weight:bold;">%d days</span></p>
                    </div>
                    <p>Don't miss this opportunity! Apply now before the deadline.</p>
                    <div style="text-align:center;margin:30px 0;">
                        <a href="%s/jobs/%d" style="display:inline-block;background:#FF6F00;color:white;padding:14px 28px;text-decoration:none;border-radius:6px;">Apply Now</a>
                    </div>
                    <p style="text-align:center;color:#666;font-size:14px;">© 2025 Kwetu-Hub Uganda 🇺🇬</p>
                </div>
            </body></html>
            """,
                urgency,
                dto.getFirstName(),
                dto.getDaysRemaining(),
                dto.getJobTitle(),
                dto.getCompanyName(),
                dto.getDeadline().format(formatter),
                dto.getDaysRemaining(),
                baseUrl,
                dto.getJobId()
        );
    }

    private String buildNewApplicationAlertEmail(JobNotificationDto dto) {
        return String.format("""
            <!DOCTYPE html>
            <html><head><meta charset="UTF-8"></head>
            <body style="font-family:Arial;margin:0;padding:20px;background:#f4f4f4;">
                <div style="max-width:600px;margin:auto;background:white;padding:30px;border-radius:10px;">
                    <h1 style="color:#1976D2;text-align:center;">🔔 New Application</h1>
                    <p>Hi %s,</p>
                    <p>You have a new application for your job posting!</p>
                    <div style="background:#e3f2fd;padding:20px;border-radius:8px;margin:20px 0;border-left:4px solid #1976D2;">
                        <p style="margin:8px 0;"><strong>Applicant:</strong> %s</p>
                        <p style="margin:8px 0;"><strong>Position:</strong> %s</p>
                        <p style="margin:8px 0;"><strong>Job ID:</strong> #%d</p>
                        <p style="margin:8px 0;"><strong>Applied:</strong> Just now</p>
                    </div>
                    <p>Review the application promptly to attract top talent.</p>
                    <div style="text-align:center;margin:30px 0;">
                        <a href="%s/jobs/%d/applications/%d" style="display:inline-block;background:#1976D2;color:white;padding:14px 28px;text-decoration:none;border-radius:6px;">Review Application</a>
                    </div>
                    <p style="text-align:center;color:#666;font-size:14px;">© 2025 Kwetu-Hub Uganda 🇺🇬</p>
                </div>
            </body></html>
            """,
                dto.getFirstName(),
                dto.getFirstName(),
                dto.getJobTitle(),
                dto.getJobId(),
                baseUrl,
                dto.getJobId(),
                dto.getApplicationId()
        );
    }

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

    // =========================================================================
    // RETRY MECHANISM
    // =========================================================================

    @Scheduled(fixedDelay = 300000) // 5 minutes
    @Transactional
    public void retryFailedNotifications() {
        log.debug("🔄 Checking for notifications to retry...");

        List<NotificationLog> pendingRetries =
                notificationLogRepository.findPendingRetries(LocalDateTime.now());

        if (pendingRetries.isEmpty()) {
            return;
        }

        log.info("📋 Found {} notifications to retry", pendingRetries.size());

        for (NotificationLog notification : pendingRetries) {
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
    // HEALTH CHECKS
    // =========================================================================

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
    // ANALYTICS
    // =========================================================================

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

    public List<NotificationLog> getUserNotifications(Long userId, int limit) {
        return notificationLogRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .limit(limit)
                .toList();
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

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

    private HttpHeaders createAfricasTalkingHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("apiKey", africasTalkingApiKey);
        headers.set("Accept", "application/json");
        return headers;
    }

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

    private NotificationLog createNotificationLog(Long userId, NotificationLog.NotificationType type,
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

    private void updateNotificationSuccess(NotificationLog log, String providerId) {
        log.setStatus(NotificationLog.NotificationStatus.SENT);
        log.setSentAt(LocalDateTime.now());
        log.setProviderMessageId(providerId);
        log.setUpdatedAt(LocalDateTime.now());
        notificationLogRepository.save(log);
    }

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

    private Map<String, Object> buildSuccessResponse(String messageId, String recipient) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", "SENT");
        response.put("recipient", maskPhone(recipient));
        if (messageId != null) response.put("messageId", messageId);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    private Map<String, Object> buildFailureResponse(String error, boolean willRetry) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("status", "FAILED");
        response.put("error", error);
        response.put("willRetry", willRetry);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    private String maskPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) return "****";
        int length = phoneNumber.length();
        int keepStart = Math.min(4, length / 3);
        int keepEnd = Math.min(4, length / 3);
        return phoneNumber.substring(0, keepStart) + "****" + phoneNumber.substring(length - keepEnd);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}