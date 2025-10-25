package com.youthconnect.notification.service;

import com.youthconnect.notification.service.dto.SmsRequest;
import com.youthconnect.notification.service.entity.NotificationLog;
import com.youthconnect.notification.service.repository.NotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * SMS SERVICE UNIT TESTS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Tests SMS delivery functionality including:
 * - Successful SMS delivery via Africa's Talking
 * - Phone number validation for Uganda
 * - Error handling and retry logic
 * - Delivery status tracking
 *
 * @author Douglas Kings Kato
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
public class SmsServiceTests {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    private com.youthconnect.notification.service.service.NotificationService notificationService;

    // Africa's Talking API mock configuration
    private static final String API_URL = "https://api.africastalking.com/version1/messaging";
    private static final String API_KEY = "test_api_key";
    private static final String USERNAME = "test_username";

    @BeforeEach
    void setUp() {
        // Initialize NotificationService with mocked dependencies
        notificationService = new com.youthconnect.notification.service.service.NotificationService(
                notificationLogRepository,
                null, // JavaMailSender not needed for SMS tests
                restTemplate
        );

        // Set Africa's Talking credentials via reflection or use test properties
        // For simplicity, we'll mock the API calls directly
    }

    /**
     * Test successful SMS delivery.
     *
     * Scenario: Valid Uganda phone number with transactional message
     * Expected: SMS sent successfully via Africa's Talking API
     */
    @Test
    void testSendSms_Success() throws ExecutionException, InterruptedException {
        // ═══════════════════════════════════════════════════════════════
        // ARRANGE
        // ═══════════════════════════════════════════════════════════════
        SmsRequest request = SmsRequest.builder()
                .recipient("+256701234567") // Valid Uganda number
                .message("Your application has been approved!")
                .messageType("TRANSACTIONAL")
                .priority(1)
                .senderId("YouthConnect")
                .userId(123L)
                .build();

        // Mock Africa's Talking API response
        Map<String, Object> apiResponse = new HashMap<>();
        Map<String, Object> smsData = new HashMap<>();
        Map<String, Object> recipient = new HashMap<>();
        recipient.put("messageId", "ATXid_test123");
        recipient.put("status", "Success");
        smsData.put("Recipients", List.of(recipient));
        apiResponse.put("SMSMessageData", smsData);

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        // Mock repository save
        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Mock RestTemplate exchange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(mockResponse);

        // ═══════════════════════════════════════════════════════════════
        // ACT
        // ═══════════════════════════════════════════════════════════════
        CompletableFuture<Map<String, Object>> resultFuture = notificationService.sendSms(request);
        Map<String, Object> result = resultFuture.get();

        // ═══════════════════════════════════════════════════════════════
        // ASSERT
        // ═══════════════════════════════════════════════════════════════
        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("status")).isEqualTo("SENT");
        assertThat(result.get("messageId")).isEqualTo("ATXid_test123");
        assertThat(result.get("recipient")).isNotNull();

        // Verify Africa's Talking API was called
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        );

        // Verify notification log was saved twice (initial + success update)
        verify(notificationLogRepository, times(2)).save(any(NotificationLog.class));
    }

    /**
     * Test SMS delivery failure due to network error.
     *
     * Scenario: Africa's Talking API unavailable
     * Expected: SMS marked as FAILED with retry scheduled
     */
    @Test
    void testSendSms_NetworkFailure() throws ExecutionException, InterruptedException {
        // ═══════════════════════════════════════════════════════════════
        // ARRANGE
        // ═══════════════════════════════════════════════════════════════
        SmsRequest request = SmsRequest.builder()
                .recipient("+256701234567")
                .message("Network failure test")
                .messageType("TRANSACTIONAL")
                .priority(1)
                .userId(456L)
                .build();

        // Mock repository save
        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Mock RestTemplate to throw exception (network error)
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenThrow(new RuntimeException("Connection timeout"));

        // ═══════════════════════════════════════════════════════════════
        // ACT
        // ═══════════════════════════════════════════════════════════════
        CompletableFuture<Map<String, Object>> resultFuture = notificationService.sendSms(request);
        Map<String, Object> result = resultFuture.get();

        // ═══════════════════════════════════════════════════════════════
        // ASSERT
        // ═══════════════════════════════════════════════════════════════
        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("status")).isEqualTo("FAILED");
        assertThat(result.get("error")).isNotNull();
        assertThat(result.get("willRetry")).isEqualTo(true); // Should schedule retry

        // Verify API was attempted
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        );

        // Verify failure was logged
        verify(notificationLogRepository, times(2)).save(any(NotificationLog.class));
    }

    /**
     * Test SMS with invalid phone number format.
     *
     * Scenario: Non-Uganda phone number format
     * Expected: Validation error before API call
     */
    @Test
    void testSendSms_InvalidPhoneNumber() throws ExecutionException, InterruptedException {
        // ═══════════════════════════════════════════════════════════════
        // ARRANGE
        // ═══════════════════════════════════════════════════════════════
        SmsRequest request = SmsRequest.builder()
                .recipient("123456") // Invalid format
                .message("Invalid phone test")
                .messageType("TRANSACTIONAL")
                .priority(1)
                .userId(789L)
                .build();

        // Mock repository save
        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ═══════════════════════════════════════════════════════════════
        // ACT
        // ═══════════════════════════════════════════════════════════════
        CompletableFuture<Map<String, Object>> resultFuture = notificationService.sendSms(request);
        Map<String, Object> result = resultFuture.get();

        // ═══════════════════════════════════════════════════════════════
        // ASSERT
        // ═══════════════════════════════════════════════════════════════
        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("error")).isNotNull();
        assertThat(result.get("error").toString()).contains("Invalid");

        // Verify API was NOT called (validation failed first)
        verify(restTemplate, never()).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }

    /**
     * Test SMS with different Uganda phone number formats.
     *
     * Scenario: Multiple valid formats (+256, 256, 0)
     * Expected: All formats normalized and accepted
     */
    @Test
    void testSendSms_UgandaPhoneFormats() throws ExecutionException, InterruptedException {
        // ═══════════════════════════════════════════════════════════════
        // ARRANGE
        // ═══════════════════════════════════════════════════════════════
        String[] validFormats = {
                "+256701234567",  // International with +
                "256701234567",   // International without +
                "0701234567"      // Local format
        };

        // Mock Africa's Talking API response
        Map<String, Object> apiResponse = new HashMap<>();
        Map<String, Object> smsData = new HashMap<>();
        Map<String, Object> recipient = new HashMap<>();
        recipient.put("messageId", "ATXid_format_test");
        recipient.put("status", "Success");
        smsData.put("Recipients", List.of(recipient));
        apiResponse.put("SMSMessageData", smsData);

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        // Mock repository save
        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Mock RestTemplate
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(mockResponse);

        // ═══════════════════════════════════════════════════════════════
        // ACT & ASSERT
        // ═══════════════════════════════════════════════════════════════
        for (String phoneFormat : validFormats) {
            SmsRequest request = SmsRequest.builder()
                    .recipient(phoneFormat)
                    .message("Format test")
                    .messageType("TRANSACTIONAL")
                    .priority(2)
                    .userId(999L)
                    .build();

            CompletableFuture<Map<String, Object>> resultFuture = notificationService.sendSms(request);
            Map<String, Object> result = resultFuture.get();

            // All formats should succeed
            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("recipient").toString()).contains("256"); // Should normalize to +256
        }

        // Verify API was called for each format
        verify(restTemplate, times(3)).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }

    /**
     * Test SMS character limit handling.
     *
     * Scenario: Long message exceeding 160 characters
     * Expected: Message sent (may be split by Africa's Talking)
     */
    @Test
    void testSendSms_LongMessage() throws ExecutionException, InterruptedException {
        // ═══════════════════════════════════════════════════════════════
        // ARRANGE
        // ═══════════════════════════════════════════════════════════════
        String longMessage = "This is a very long SMS message that exceeds the standard 160 character limit. "
                + "It should be handled by Africa's Talking API which will automatically split it into "
                + "multiple SMS messages if necessary. This tests our integration.";

        SmsRequest request = SmsRequest.builder()
                .recipient("+256701234567")
                .message(longMessage)
                .messageType("TRANSACTIONAL")
                .priority(1)
                .userId(111L)
                .build();

        // Mock API response
        Map<String, Object> apiResponse = new HashMap<>();
        Map<String, Object> smsData = new HashMap<>();
        Map<String, Object> recipient = new HashMap<>();
        recipient.put("messageId", "ATXid_long_msg");
        recipient.put("status", "Success");
        smsData.put("Recipients", List.of(recipient));
        apiResponse.put("SMSMessageData", smsData);

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        // Mock dependencies
        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(mockResponse);

        // ═══════════════════════════════════════════════════════════════
        // ACT
        // ═══════════════════════════════════════════════════════════════
        CompletableFuture<Map<String, Object>> resultFuture = notificationService.sendSms(request);
        Map<String, Object> result = resultFuture.get();

        // ═══════════════════════════════════════════════════════════════
        // ASSERT
        // ═══════════════════════════════════════════════════════════════
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(longMessage.length()).isGreaterThan(160); // Verify it's actually long

        // Verify message was sent despite length
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }
}