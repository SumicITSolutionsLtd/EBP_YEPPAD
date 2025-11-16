package com.youthconnect.notification.service;

import com.youthconnect.notification.service.dto.SmsRequest;
import com.youthconnect.notification.service.entity.NotificationLog;
import com.youthconnect.notification.service.repository.NotificationLogRepository;
import com.youthconnect.notification.service.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * SMS SERVICE UNIT TESTS (FIXED - UUID SUPPORT)
 * ═══════════════════════════════════════════════════════════════════════════
 */
@ExtendWith(MockitoExtension.class)
public class SmsServiceTests {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationLogRepository,
                null,
                restTemplate
        );
    }

    @Test
    void testSendSms_Success() throws ExecutionException, InterruptedException {
        UUID testUserId = UUID.randomUUID();

        SmsRequest request = SmsRequest.builder()
                .recipient("+256701234567")
                .message("Your application has been approved!")
                .messageType("TRANSACTIONAL")
                .priority(1)
                .senderId("YouthConnect")
                .userId(testUserId)
                .build();

        Map<String, Object> apiResponse = new HashMap<>();
        Map<String, Object> smsData = new HashMap<>();
        Map<String, Object> recipient = new HashMap<>();
        recipient.put("messageId", "ATXid_test123");
        recipient.put("status", "Success");
        smsData.put("Recipients", List.of(recipient));
        apiResponse.put("SMSMessageData", smsData);

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(mockResponse);

        CompletableFuture<Map<String, Object>> resultFuture = notificationService.sendSms(request);
        Map<String, Object> result = resultFuture.get();

        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("status")).isEqualTo("SENT");
        assertThat(result.get("messageId")).isEqualTo("ATXid_test123");

        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        );

        verify(notificationLogRepository, times(2)).save(any(NotificationLog.class));
    }

    @Test
    void testSendSms_NetworkFailure() throws ExecutionException, InterruptedException {
        UUID testUserId = UUID.randomUUID();

        SmsRequest request = SmsRequest.builder()
                .recipient("+256701234567")
                .message("Network failure test")
                .messageType("TRANSACTIONAL")
                .priority(1)
                .userId(testUserId)
                .build();

        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenThrow(new RuntimeException("Connection timeout"));

        CompletableFuture<Map<String, Object>> resultFuture = notificationService.sendSms(request);
        Map<String, Object> result = resultFuture.get();

        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(false);
        assertThat(result.get("status")).isEqualTo("FAILED");
        assertThat(result.get("willRetry")).isEqualTo(true);

        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        );

        verify(notificationLogRepository, times(2)).save(any(NotificationLog.class));
    }

    @Test
    void testSendSms_UgandaPhoneFormats() throws ExecutionException, InterruptedException {
        UUID testUserId = UUID.randomUUID();

        String[] validFormats = {
                "+256701234567",
                "256701234567",
                "0701234567"
        };

        Map<String, Object> apiResponse = new HashMap<>();
        Map<String, Object> smsData = new HashMap<>();
        Map<String, Object> recipient = new HashMap<>();
        recipient.put("messageId", "ATXid_format_test");
        recipient.put("status", "Success");
        smsData.put("Recipients", List.of(recipient));
        apiResponse.put("SMSMessageData", smsData);

        ResponseEntity<Map> mockResponse = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(mockResponse);

        for (String phoneFormat : validFormats) {
            SmsRequest request = SmsRequest.builder()
                    .recipient(phoneFormat)
                    .message("Format test")
                    .messageType("TRANSACTIONAL")
                    .priority(2)
                    .userId(testUserId)
                    .build();

            CompletableFuture<Map<String, Object>> resultFuture = notificationService.sendSms(request);
            Map<String, Object> result = resultFuture.get();

            assertThat(result.get("success")).isEqualTo(true);
            assertThat(result.get("recipient").toString()).contains("256");
        }

        verify(restTemplate, times(3)).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }
}