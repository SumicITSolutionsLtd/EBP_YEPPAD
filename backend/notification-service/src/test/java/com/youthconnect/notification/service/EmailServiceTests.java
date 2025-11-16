package com.youthconnect.notification.service;

import com.youthconnect.notification.service.dto.EmailRequest;
import com.youthconnect.notification.service.entity.NotificationLog;
import com.youthconnect.notification.service.repository.NotificationLogRepository;
import com.youthconnect.notification.service.service.NotificationService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * EMAIL SERVICE UNIT TESTS (FIXED - UUID SUPPORT)
 * ═══════════════════════════════════════════════════════════════════════════
 */
@ExtendWith(MockitoExtension.class)
public class EmailServiceTests {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private MimeMessage mimeMessage;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationLogRepository,
                mailSender,
                null // RestTemplate not needed for email tests
        );
    }

    @Test
    void testSendEmail_Success() throws ExecutionException, InterruptedException {
        // ✅ FIXED: Use UUID instead of Long
        UUID testUserId = UUID.randomUUID();

        EmailRequest request = EmailRequest.builder()
                .recipient("test@example.com")
                .subject("Test Email Subject")
                .textContent("This is a test email message.")
                .htmlContent("<html><body><h1>Test Email</h1><p>This is a test.</p></body></html>")
                .userId(testUserId) // ✅ UUID instead of Long
                .build();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(mailSender).send(any(MimeMessage.class));

        CompletableFuture<Map<String, Object>> resultFuture = notificationService.sendEmail(request);
        Map<String, Object> result = resultFuture.get();

        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("status")).isEqualTo("SENT");
        assertThat(result.get("recipient")).isEqualTo("test@example.com");

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(notificationLogRepository, times(2)).save(any(NotificationLog.class));
    }

    @Test
    void testSendEmail_SmtpFailure() throws ExecutionException, InterruptedException {
        UUID testUserId = UUID.randomUUID();

        EmailRequest request = EmailRequest.builder()
                .recipient("failure@example.com")
                .subject("Test Failure")
                .textContent("This should fail")
                .userId(testUserId)
                .build();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("SMTP server unavailable"))
                .when(mailSender).send(any(MimeMessage.class));

        CompletableFuture<Map<String, Object>> resultFuture = notificationService.sendEmail(request);
        Map<String, Object> result = resultFuture.get();

        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("status")).isEqualTo("FAILED");
        assertThat(result.get("error")).isNotNull();

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(notificationLogRepository, times(2)).save(any(NotificationLog.class));
    }

    @Test
    void testSendEmail_HtmlContent() throws ExecutionException, InterruptedException {
        UUID testUserId = UUID.randomUUID();

        EmailRequest request = EmailRequest.builder()
                .recipient("html@example.com")
                .subject("HTML Email Test")
                .textContent("Plain text version")
                .htmlContent("<html><body><h1>HTML Version</h1><p>Rich content</p></body></html>")
                .userId(testUserId)
                .build();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(mailSender).send(any(MimeMessage.class));

        CompletableFuture<Map<String, Object>> resultFuture = notificationService.sendEmail(request);
        Map<String, Object> result = resultFuture.get();

        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("status")).isEqualTo("SENT");

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendEmail_InvalidRecipient() throws ExecutionException, InterruptedException {
        UUID testUserId = UUID.randomUUID();

        EmailRequest request = EmailRequest.builder()
                .recipient("invalid-email")
                .subject("Test")
                .textContent("Test message")
                .userId(testUserId)
                .build();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("Invalid email address"))
                .when(mailSender).send(any(MimeMessage.class));

        CompletableFuture<Map<String, Object>> resultFuture = notificationService.sendEmail(request);
        Map<String, Object> result = resultFuture.get();

        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isNotNull();
    }
}