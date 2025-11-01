
package com.serviceprovider.serviceprovider.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.serviceprovider.serviceprovider.model.Status;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
public class ServiceProvider {
    @Id
    private UUID id;

    @NonNull
    private String providerName;

    private String email;

    private String password;

    private String AreaOfExpertise;

    private String location;

    @NonNull
    @Enumerated(EnumType.STRING)
    private Status status;

    private Long Description;

    private LocalDateTime createdAt;
}