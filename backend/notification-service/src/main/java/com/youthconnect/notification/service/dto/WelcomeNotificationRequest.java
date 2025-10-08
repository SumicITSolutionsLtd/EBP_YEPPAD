package com.youthconnect.notification.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WelcomeNotificationRequest {
    private Long userId;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String userRole;
    private String preferredLanguage; // en, lg, lur, lgb
}
