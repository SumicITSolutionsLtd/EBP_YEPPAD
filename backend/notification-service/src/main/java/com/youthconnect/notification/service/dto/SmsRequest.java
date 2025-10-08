package com.youthconnect.notification.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {
    private String recipient;      // +256... format
    private String message;
    private String messageType;    // TRANSACTIONAL, PROMOTIONAL
    private Integer priority;      // 1-HIGH, 2-MEDIUM, 3-LOW
    private String senderId;
    private Long userId;           // For logging
}
