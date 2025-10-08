package com.youthconnect.notification.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String recipient;
    private String subject;
    private String htmlContent;
    private String textContent;
    private Long userId;
}
