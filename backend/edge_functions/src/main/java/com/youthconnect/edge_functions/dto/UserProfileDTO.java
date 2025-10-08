package com.youthconnect.edge_functions.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
    private Long userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    // Add fields that match your user-service DTO
    private String email;
    private String userType;
    private Boolean verified;
}