package com.youthconnect.edge_functions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * USSD Session DTO
 * Manages stateful USSD interactions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UssdSessionDTO {
    private String sessionId;
    private String phoneNumber;
    private String currentMenu;
    private Map<String, Object> sessionData;
    private String language;
    private Integer menuDepth;
    private LocalDateTime expiresAt;
}