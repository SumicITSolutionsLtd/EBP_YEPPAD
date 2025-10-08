package com.youthconnect.ussd_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for profile update requests from USSD service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequestDTO {
    private String firstName;
    private String lastName;
}