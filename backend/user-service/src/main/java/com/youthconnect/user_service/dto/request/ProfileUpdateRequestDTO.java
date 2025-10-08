package com.youthconnect.user_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for handling profile update requests originating from the USSD service.
 * It contains ONLY the fields that can be updated via the USSD menu (first name and last name).
 * This class name now perfectly matches the file name.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequestDTO {
    private String firstName;
    private String lastName;
}
