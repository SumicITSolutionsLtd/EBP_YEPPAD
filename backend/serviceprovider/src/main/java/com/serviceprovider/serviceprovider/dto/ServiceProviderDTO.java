package com.serviceprovider.serviceprovider.dto;

import com.serviceprovider.serviceprovider.model.Status;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ServiceProviderDTO {
    private UUID providerId;
    private String providerName;
    private String email;
    private String password;
    private String AreaOfExpertise;
    private Status status;
    private Long Description;
    LocalDateTime localDateTime = LocalDateTime.now();
    private LocalDateTime createdAt = localDateTime;

}
