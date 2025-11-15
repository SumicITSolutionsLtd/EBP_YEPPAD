package com.serviceprovider.serviceprovider.service;

import com.serviceprovider.serviceprovider.dto.ServiceProviderDTO;
import com.serviceprovider.serviceprovider.entity.ServiceProvider;
import com.serviceprovider.serviceprovider.model.Status;
import com.serviceprovider.serviceprovider.repository.ServiceProviderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class ServiceProviderService {

    @Autowired
    private ServiceProviderRepository providerRepository;

    public Page<ServiceProvider> getAllServiceProviders(Status status, int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        if (status!= null){
            return providerRepository.findByStatus(status, pageable);
        }else {
            return providerRepository.findAll(pageable);
        }
    }

    public ServiceProvider create(ServiceProvider serviceProvider){
        if (providerRepository.existsByEmail(serviceProvider.getEmail())){
            throw new RuntimeException("Email already in use");
        }
        serviceProvider.setCreatedAt(LocalDateTime.now());
        serviceProvider.setProviderName(serviceProvider.getProviderName());
        serviceProvider.setEmail(serviceProvider.getEmail());
        serviceProvider.setLocation(serviceProvider.getLocation());
        serviceProvider.setPassword(serviceProvider.getPassword());
        serviceProvider.setAreaOfExpertise(serviceProvider.getAreaOfExpertise());
        serviceProvider.setDescription(serviceProvider.getDescription());
        serviceProvider.setStatus(Status.NEW);

        return providerRepository.saveAndFlush(serviceProvider);
    }

    public ServiceProvider update(UUID id, ServiceProvider updatedServiceProvider){
        if (!providerRepository.existsByEmail(updatedServiceProvider.getEmail())){
            throw new RuntimeException("Email doesnt exist");
        }
        updatedServiceProvider.setProviderName(updatedServiceProvider.getProviderName());
        updatedServiceProvider.setEmail(updatedServiceProvider.getEmail());
        updatedServiceProvider.setLocation(updatedServiceProvider.getLocation());
        updatedServiceProvider.setDescription(updatedServiceProvider.getDescription());
        updatedServiceProvider.setType(updatedServiceProvider.getType());
        return providerRepository.saveAndFlush(updatedServiceProvider);
    }

    public Page<ServiceProvider> updateProviders(Page<ServiceProvider> providers){
        List<ServiceProvider> updatedProviders = providers.stream()
                .map(provider -> providerRepository.findById(provider.getId())
                        .map(existing -> {
                            existing.setProviderName(existing.getProviderName());
                            existing.setEmail(existing.getEmail());
                            existing.setLocation(existing.getLocation());
                            existing.setDescription(existing.getDescription());
                            existing.setType(existing.getType());
                            return providerRepository.save(existing);
                        })
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(updatedProviders, providers.getPageable(), updatedProviders.size());
    }


    public ServiceProvider getById(UUID providerId){
        return providerRepository.findById(providerId).orElseThrow(()-> new RuntimeException("Cannot find user with Id: " + providerId));
    }



    public ServiceProvider getByProviderName(String name){
        return providerRepository.findByProviderName(name).orElseThrow(()-> new RuntimeException("Cannot find user with that name: " + name));}

}
