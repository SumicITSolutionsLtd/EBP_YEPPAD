package com.youthconnect.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youthconnect.notification.service.controller.NotificationController;
import com.youthconnect.notification.service.dto.*;
import com.youthconnect.notification.service.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * NOTIFICATION CONTROLLER INTEGRATION TESTS (FIXED - UUID SUPPORT)
 * ═══════════════════════════════════════════════════════════════════════════
 */
@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @Test
    void testSendSms_Success() throws Exception {
        // ✅ FIXED: Use UUID
        UUID testUserId = UUID.randomUUID();

        SmsRequest request = SmsRequest.builder()
                .recipient("+256701234567")
                .message("Test SMS")
                .messageType("TRANSACTIONAL")
                .priority(1)
                .userId(testUserId)
                .build();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("messageId", "ATX123");

        when(notificationService.sendSms(any(SmsRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        mockMvc.perform(post("/api/notifications/sms/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testSendEmail_Success() throws Exception {
        UUID testUserId = UUID.randomUUID();

        EmailRequest request = EmailRequest.builder()
                .recipient("test@example.com")
                .subject("Test Email")
                .textContent("Test content")
                .userId(testUserId)
                .build();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        when(notificationService.sendEmail(any(EmailRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        mockMvc.perform(post("/api/notifications/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testSendWelcomeNotification_Success() throws Exception {
        UUID testUserId = UUID.randomUUID();

        WelcomeNotificationRequest request = WelcomeNotificationRequest.builder()
                .userId(testUserId)
                .email("test@example.com")
                .phoneNumber("+256701234567")
                .firstName("John")
                .userRole("YOUTH")
                .build();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("sms", Map.of("success", true));
        response.put("email", Map.of("success", true));

        when(notificationService.sendWelcomeNotification(any(WelcomeNotificationRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        mockMvc.perform(post("/api/notifications/welcome")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testHealthCheck_Success() throws Exception {
        Map<String, Object> smsHealth = Map.of(
                "healthy", true,
                "status", "UP",
                "provider", "AFRICAS_TALKING",
                "responseTime", 150
        );

        Map<String, Object> emailHealth = Map.of(
                "healthy", true,
                "status", "UP",
                "provider", "SMTP",
                "responseTime", 80
        );

        when(notificationService.checkSmsServiceHealth()).thenReturn(smsHealth);
        when(notificationService.checkEmailServiceHealth()).thenReturn(emailHealth);

        mockMvc.perform(get("/api/notifications/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.checks.sms.status").value("UP"))
                .andExpect(jsonPath("$.checks.email.status").value("UP"));
    }

    @Test
    void testHealthCheck_SmsDown() throws Exception {
        Map<String, Object> smsHealth = Map.of(
                "healthy", false,
                "status", "DOWN",
                "error", "Connection timeout"
        );

        Map<String, Object> emailHealth = Map.of(
                "healthy", true,
                "status", "UP"
        );

        when(notificationService.checkSmsServiceHealth()).thenReturn(smsHealth);
        when(notificationService.checkEmailServiceHealth()).thenReturn(emailHealth);

        mockMvc.perform(get("/api/notifications/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEGRADED"))
                .andExpect(jsonPath("$.checks.sms.status").value("DOWN"))
                .andExpect(jsonPath("$.checks.email.status").value("UP"));
    }

    @Test
    void testSendUssdConfirmation_Success() throws Exception {
        UssdConfirmationRequest request = UssdConfirmationRequest.builder()
                .phoneNumber("+256701234567")
                .userName("John Doe")
                .confirmationCode("ABC123")
                .build();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        when(notificationService.sendUssdConfirmation(any(UssdConfirmationRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        mockMvc.perform(post("/api/notifications/ussd/confirmation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}