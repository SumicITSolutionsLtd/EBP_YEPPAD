package com.serviceprovider.serviceprovider.service;

import com.serviceprovider.serviceprovider.dto.ServiceProviderDTO;
import com.serviceprovider.serviceprovider.entity.ServiceProvider;
import com.serviceprovider.serviceprovider.model.Status;
import com.serviceprovider.serviceprovider.repository.ServiceProviderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public ServiceProvider getById(UUID providerId){
        return providerRepository.findById(providerId).orElseThrow(()-> new RuntimeException("Cannot find user with Id: " + providerId));
    }

    public ServiceProvider getByProviderName(String name){
        return providerRepository.findByProviderName(name).orElseThrow(()-> new RuntimeException("Cannot find user with that name: " + name));}

}
