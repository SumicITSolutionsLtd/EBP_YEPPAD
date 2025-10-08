package com.youthconnect.ussd_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user profile responses from user service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long userId;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String gender;
    private String role;
    private String district;
    private String businessStage;
    private String ageGroup;
    private boolean isActive;

    // Constructor with essential fields
    public UserProfileDTO(String phoneNumber, String firstName, String lastName) {
        this.phoneNumber = phoneNumber;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}