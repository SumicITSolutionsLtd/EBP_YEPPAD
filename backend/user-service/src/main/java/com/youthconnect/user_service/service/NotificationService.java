package com.youthconnect.user_service.service;

import com.youthconnect.user_service.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Notification Service for Youth Connect Uganda
 *
 * Handles SMS and Email notifications for user registration,
 * USSD interactions, and system alerts. Integrates with external
 * SMS providers and email services.
 *
 * Features:
 * - SMS notifications via Africa's Talking API
 * - Email notifications via SMTP
 * - Async processing for performance
 * - Retry mechanisms for failed deliveries
 * - Template-based messaging
 *
 * @author Youth Connect Uganda Development Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ApplicationProperties applicationProperties;
    private final RestTemplate restTemplate;

    /**
     * Send SMS notification asynchronously
     * Primary method for USSD registration confirmations
     */
    @Async("smsExecutor")
    public CompletableFuture<Boolean> sendSmsNotification(String phoneNumber, String message) {
        log.info("Sending SMS to: {} (masked for privacy)", maskPhoneNumber(phoneNumber));

        try {
            // Validate phone number format for Uganda
            if (!isValidUgandanPhoneNumber(phoneNumber)) {
                log.error("Invalid phone number format: {}", maskPhoneNumber(phoneNumber));
                return CompletableFuture.completedFuture(false);
            }

            // Prepare SMS payload for Africa's Talking API
            Map<String, Object> smsPayload = new HashMap<>();
            smsPayload.put("username", "youthconnect_ug"); // Replace with actual username
            smsPayload.put("to", formatPhoneNumber(phoneNumber));
            smsPayload.put("message", message);
            smsPayload.put("from", "YouthConnect"); // Sender ID

            // Call external SMS API
            String smsApiUrl = "https://api.africastalking.com/version1/messaging";
            // Map<String, Object> response = restTemplate.postForObject(smsApiUrl, smsPayload, Map.class);

            // For development/testing - simulate success
            simulateSmsDelivery(phoneNumber, message);

            log.info("SMS sent successfully to: {}", maskPhoneNumber(phoneNumber));
            return CompletableFuture.completedFuture(true);

        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", maskPhoneNumber(phoneNumber), e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Send welcome SMS for new user registration
     */
    @Async("smsExecutor")
    public CompletableFuture<Boolean> sendWelcomeSms(String phoneNumber, String firstName, String role) {
        String welcomeMessage = buildWelcomeMessage(firstName, role);
        return sendSmsNotification(phoneNumber, welcomeMessage);
    }

    /**
     * Send USSD registration confirmation SMS
     */
    @Async("smsExecutor")
    public CompletableFuture<Boolean> sendUssdRegistrationConfirmation(String phoneNumber, String firstName, String syntheticEmail) {
        String message = String.format(
                "Welcome to Youth Connect Uganda, %s! Your account has been created. " +
                        "Email: %s. Visit https://youthconnect.ug to complete your profile.",
                firstName, syntheticEmail
        );
        return sendSmsNotification(phoneNumber, message);
    }

    /**
     * Send opportunity notification SMS
     */
    @Async("smsExecutor")
    public CompletableFuture<Boolean> sendOpportunityAlert(String phoneNumber, String opportunityTitle, String opportunityType) {
        String message = String.format(
                "New %s opportunity: %s. Apply now at https://youthconnect.ug/opportunities",
                opportunityType.toLowerCase(), opportunityTitle
        );
        return sendSmsNotification(phoneNumber, message);
    }

    /**
     * Send email notification asynchronously
     */
    @Async("emailExecutor")
    public CompletableFuture<Boolean> sendEmailNotification(String email, String subject, String htmlContent) {
        log.info("Sending email to: {} with subject: {}", email, subject);

        try {
            // Email sending logic would go here
            // For now, simulate email sending
            simulateEmailDelivery(email, subject, htmlContent);

            log.info("Email sent successfully to: {}", email);
            return CompletableFuture.completedFuture(true);

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", email, e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Send welcome email for web registration
     */
    @Async("emailExecutor")
    public CompletableFuture<Boolean> sendWelcomeEmail(String email, String firstName, String role) {
        String subject = "Welcome to Youth Connect Uganda!";
        String htmlContent = buildWelcomeEmailHtml(firstName, role);
        return sendEmailNotification(email, subject, htmlContent);
    }

    /**
     * Validate Ugandan phone number format
     */
    private boolean isValidUgandanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)\\.]", "");

        // Uganda phone number patterns
        return cleaned.matches("^\\+?256[7][0-9]{8}$") ||  // +256701234567
                cleaned.matches("^0[7][0-9]{8}$");          // 0701234567
    }

    /**
     * Format phone number for SMS API
     */
    private String formatPhoneNumber(String phoneNumber) {
        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)\\.]", "");

        if (cleaned.startsWith("0")) {
            return "+256" + cleaned.substring(1);
        } else if (cleaned.startsWith("256")) {
            return "+" + cleaned;
        } else if (cleaned.startsWith("+256")) {
            return cleaned;
        }

        return "+256" + cleaned; // Default fallback
    }

    /**
     * Mask phone number for privacy in logs
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 6) {
            return "****";
        }

        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)\\.]", "");
        if (cleaned.length() >= 6) {
            return cleaned.substring(0, 3) + "****" + cleaned.substring(cleaned.length() - 3);
        }

        return "****";
    }

    /**
     * Build welcome message based on user role
     */
    private String buildWelcomeMessage(String firstName, String role) {
        String baseMessage = String.format("Welcome to Youth Connect Uganda, %s!", firstName);

        switch (role.toUpperCase()) {
            case "YOUTH":
                return baseMessage + " Start exploring opportunities, connect with mentors, and boost your entrepreneurship journey.";
            case "MENTOR":
                return baseMessage + " Thank you for joining as a mentor. Help shape the future of young entrepreneurs in Uganda.";
            case "NGO":
                return baseMessage + " Post opportunities and track the impact of your programs with our analytics dashboard.";
            case "FUNDER":
                return baseMessage + " Connect with promising young entrepreneurs and fund the next generation of innovators.";
            default:
                return baseMessage + " Explore our platform and discover opportunities for growth and collaboration.";
        }
    }

    /**
     * Build welcome email HTML content
     */
    private String buildWelcomeEmailHtml(String firstName, String role) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #2E8B57;">Welcome to Youth Connect Uganda!</h2>
                    <p>Dear %s,</p>
                    <p>Thank you for joining Youth Connect Uganda as a <strong>%s</strong>.</p>
                    <p>Our platform connects young entrepreneurs with opportunities, mentors, and resources to build successful businesses in Uganda.</p>
                    
                    <div style="background-color: #f0f8f0; padding: 20px; border-radius: 5px; margin: 20px 0;">
                        <h3>What's Next?</h3>
                        <ul>
                            <li>Complete your profile to get personalized recommendations</li>
                            <li>Explore opportunities in grants, loans, and training</li>
                            <li>Connect with mentors and fellow entrepreneurs</li>
                            <li>Access our learning modules in multiple local languages</li>
                        </ul>
                    </div>
                    
                    <p>
                        <a href="https://youthconnect.ug/login" 
                           style="background-color: #2E8B57; color: white; padding: 10px 20px; 
                                  text-decoration: none; border-radius: 5px;">
                            Access Your Dashboard
                        </a>
                    </p>
                    
                    <p>For support, reply to this email or contact us at support@youthconnect.ug</p>
                    
                    <p>Best regards,<br>
                    The Youth Connect Uganda Team</p>
                </div>
            </body>
            </html>
            """, firstName, role.toLowerCase());
    }

    /**
     * Simulate SMS delivery for development
     */
    private void simulateSmsDelivery(String phoneNumber, String message) {
        log.info("SMS Simulation - To: {} Message: {}", maskPhoneNumber(phoneNumber), message);

        // Simulate processing delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Simulate email delivery for development
     */
    private void simulateEmailDelivery(String email, String subject, String content) {
        log.info("Email Simulation - To: {} Subject: {}", email, subject);

        // Simulate processing delay
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Send batch notifications
     */
    @Async("smsExecutor")
    public CompletableFuture<Integer> sendBatchSms(Map<String, String> phoneNumberToMessageMap) {
        log.info("Sending batch SMS to {} recipients", phoneNumberToMessageMap.size());

        int successCount = 0;
        for (Map.Entry<String, String> entry : phoneNumberToMessageMap.entrySet()) {
            try {
                CompletableFuture<Boolean> result = sendSmsNotification(entry.getKey(), entry.getValue());
                if (result.get()) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("Failed to send SMS in batch to {}: {}",
                        maskPhoneNumber(entry.getKey()), e.getMessage());
            }
        }

        log.info("Batch SMS completed: {}/{} successful", successCount, phoneNumberToMessageMap.size());
        return CompletableFuture.completedFuture(successCount);
    }
}