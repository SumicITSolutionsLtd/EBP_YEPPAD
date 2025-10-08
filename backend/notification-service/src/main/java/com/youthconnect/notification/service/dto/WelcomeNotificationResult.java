package com.youthconnect.notification.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WelcomeNotificationResult {
    private boolean emailSent;
    private boolean smsSent;
}

