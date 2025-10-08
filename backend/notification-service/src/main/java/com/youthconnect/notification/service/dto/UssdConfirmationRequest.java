package com.youthconnect.notification.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UssdConfirmationRequest {
    private String phoneNumber;
    private String userName;
    private String confirmationCode;
    private String message;
}
