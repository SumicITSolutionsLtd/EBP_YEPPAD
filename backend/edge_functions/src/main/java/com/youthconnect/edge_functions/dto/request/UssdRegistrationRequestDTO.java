package com.youthconnect.edge_functions.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for USSD registration requests containing minimal user information
 * collected via USSD interface. This matches the structure used in user-service.
 */

/**
 * USSD Registration Request DTO
 * Contains minimal data collected via USSD interface
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UssdRegistrationRequestDTO {
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String gender;
    private String ageGroup;
    private String district;
    private String businessStage;
}
