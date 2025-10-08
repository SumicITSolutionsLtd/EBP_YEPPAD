package com.youthconnect.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UssdLoginRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?256[0-9]{9}$|^0[0-9]{9}$", message = "Invalid Ugandan phone number")
    private String phoneNumber;

    private String sessionId; // USSD session identifier
}
