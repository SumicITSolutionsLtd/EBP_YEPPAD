
package com.serviceprovider.serviceprovider.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.serviceprovider.serviceprovider.model.ProviderType;
import com.serviceprovider.serviceprovider.model.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
public class ServiceProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String providerName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String AreaOfExpertise;

    private String location;

//    @NonNull
    @Enumerated(EnumType.STRING)
    private Status status;

//    @NonNull
    @Enumerated(EnumType.STRING)
    private ProviderType type;

    private Long Description;

    private LocalDateTime createdAt;
}