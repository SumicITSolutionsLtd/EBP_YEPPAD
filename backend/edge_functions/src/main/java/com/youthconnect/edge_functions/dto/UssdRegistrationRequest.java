package com.youthconnect.edge_functions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UssdRegistrationRequest {
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String gender;
    private String ageGroup;
    private String district;
    private String businessStage;
}
