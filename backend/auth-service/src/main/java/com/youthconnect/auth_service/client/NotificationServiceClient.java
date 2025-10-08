package com.youthconnect.auth_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "notification-service", path = "/api/notifications")
public interface NotificationServiceClient {

    @PostMapping("/welcome-email")
    void sendWelcomeEmail(@RequestParam String email, @RequestParam String role);

    @PostMapping("/sms")
    void sendSms(@RequestParam String phone, @RequestParam String message);
}
