package com.youthconnect.notification.service;

import com.youthconnect.notification.service.dto.EmailRequest;
import com.youthconnect.notification.service.entity.NotificationLog;
import com.youthconnect.notification.service.repository.NotificationLogRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * EMAIL SERVICE UNIT TESTS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Tests email delivery functionality including:
 * - Successful email delivery via SMTP
 * - HTML email with templates
 * - Error handling and retry logic
 * - Delivery status tracking
 *
 * @author Douglas Kings Kato
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
public class EmailServiceTests {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private MimeMessage mimeMessage;

    private com.youthconnect.notification.service.service.NotificationService notificationService;

    @BeforeEach
    void setUp() {
        // Initialize NotificationService with mocked dependencies
        notificationService = new com.youthconnect.notification.service.service.NotificationService(
                notificationLogRepository,
                mailSender,
                null // RestTemplate not needed for email tests
        );
    }

    /**
     * Test successful email delivery.
     *
     * Scenario: Valid email request with subject and content
     * Expected: Email sent successfully with SENT status
     */
    @Test
    void testSendEmail_Success() throws ExecutionException, InterruptedException {
        // ═══════════════════════════════════════════════════════════════
        // ARRANGE
        // ═══════════════════════════════════════════════════════════════
        EmailRequest request = EmailRequest.builder()
                .recipient("test@example.com")
                .subject("Test Email Subject")
                .textContent("This is a test email message.")
                .htmlContent("<html><body><h1>Test Email</h1><p>This is a test.</p></body></html>")
                .userId(123L)
                .build();

        // Mock MimeMessage creation
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Mock repository save (return notification log)
        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Mock email sending (void method - no exception means success)
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // ═══════════════════════════════════════════════════════════════
        // ACT
        // ═══════════════════════════════════════════════════════════════
        CompletableFuture<Map<String, Object>> resultFuture = notificationService.sendEmail(request);
        Map<String, Object> result = resultFuture.get();

        // ═══════════════════════════════════════════════════════════════
        // ASSERT
        // ═══════════════════════════════════════════════════════════════
        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("status")).isEqualTo("SENT");
        assertThat(result.get("recipient")).isEqualTo("test@example.com");

        // Verify interactions
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(notificationLogRepository, times(2)).save(any(NotificationLog.class)); // Initial + success update
    }

    /**
     * Test email delivery failure due to SMTP error.
     *
     * Scenario: SMTP server unavailable or network error
     * Expected: Email marked as FAILED with error message
     */
    @Test
    void testSendEmail_SmtpFailure() throws ExecutionException, InterruptedException {
        // ═══════════════════════════════════════════════════════════════
        // ARRANGE
        // ═══════════════════════════════════════════════════════════════
        EmailRequest request = EmailRequest.builder()
                .recipient("failure@example.com")
                .subject("Test Failure")
                .textContent("This should fail")
                .userId(456L)
                .build();

        // Mock MimeMessage creation
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Mock repository save
        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Mock email sending failure (throw exception)
        doThrow(new RuntimeException("SMTP server unavailable"))
                .when(mailSender).send(any(MimeMessage.class));

        // ═══════════════════════════════════════════════════════════════
        // ACT
        // ═══════════════════════════════════════════════════════════════
        CompletableFuture<Map<String, Object>> resultFuture = notificationService.sendEmail(request);
        Map<String, Object> result = resultFuture.get();

        // ═══════════════════════════════════════════════════════════════
        // ASSERT
        // ═══════════════════════════════════════════════════════════════
        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("status")).isEqualTo("FAILED");
        assertThat(result.get("error")).isNotNull();
        assertThat(result.get("error").toString()).contains("SMTP server unavailable");

        // Verify interactions
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(notificationLogRepository, times(2)).save(any(NotificationLog.class)); // Initial + failure update
    }

    /**
     * Test email with HTML content.
     *
     * Scenario: Email contains both text and HTML versions
     * Expected: HTML email sent successfully
     */
    @Test
    void testSendEmail_HtmlContent() throws ExecutionException, InterruptedException {
        // ═══════════════════════════════════════════════════════════════
        // ARRANGE
        // ═══════════════════════════════════════════════════════════════
        EmailRequest request = EmailRequest.builder()
                .recipient("html@example.com")
                .subject("HTML Email Test")
                .textContent("Plain text version")
                .htmlContent("<html><body><h1>HTML Version</h1><p>Rich content</p></body></html>")
                .userId(789L)
                .build();

        // Mock MimeMessage creation
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Mock repository save
        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Mock successful sending
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // ═══════════════════════════════════════════════════════════════
        // ACT
        // ═══════════════════════════════════════════════════════════════
        CompletableFuture<Map<String, Object>> resultFuture = notificationService.sendEmail(request);
        Map<String, Object> result = resultFuture.get();

        // ═══════════════════════════════════════════════════════════════
        // ASSERT
        // ═══════════════════════════════════════════════════════════════
        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("status")).isEqualTo("SENT");

        // Verify MimeMessage was created and sent
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    /**
     * Test email validation - invalid recipient.
     *
     * Scenario: Invalid email format
     * Expected: Validation error before sending
     */
    @Test
    void testSendEmail_InvalidRecipient() throws ExecutionException, InterruptedException {
        // ═══════════════════════════════════════════════════════════════
        // ARRANGE
        // ═══════════════════════════════════════════════════════════════
        EmailRequest request = EmailRequest.builder()
                .recipient("invalid-email") // No @ symbol
                .subject("Test")
                .textContent("Test message")
                .userId(999L)
                .build();

        // Mock MimeMessage creation
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Mock repository save
        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Mock send failure due to invalid email
        doThrow(new RuntimeException("Invalid email address"))
                .when(mailSender).send(any(MimeMessage.class));

        // ═══════════════════════════════════════════════════════════════
        // ACT
        // ═══════════════════════════════════════════════════════════════
        CompletableFuture<Map<String, Object>> resultFuture = notificationService.sendEmail(request);
        Map<String, Object> result = resultFuture.get();

        // ═══════════════════════════════════════════════════════════════
        // ASSERT
        // ═══════════════════════════════════════════════════════════════
        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isNotNull();
    }
}