package com.youthconnect.edge_functions.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Notification Request DTO
 * Used for sending multi-channel notifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private Long userId;
    private String type;  // SMS, EMAIL, PUSH, IN_APP
    private String channel;
    private String recipient;
    private String subject;
    private String content;
    private Map<String, Object> templateData;
}
