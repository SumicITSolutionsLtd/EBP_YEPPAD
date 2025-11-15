package com.serviceprovider.serviceprovider.repository;

import com.serviceprovider.serviceprovider.dto.ServiceProviderDTO;

import com.serviceprovider.serviceprovider.entity.ServiceProvider;
import com.serviceprovider.serviceprovider.model.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, UUID> {
    Optional<ServiceProvider> findById(UUID uuid);
    Optional<ServiceProvider> findByProviderName(String providerName);
    Page<ServiceProvider> findAll(Pageable pageable);
    Page<ServiceProvider>findByStatus(Status status, Pageable pageable);
    Boolean existsByEmail(String email);
}
