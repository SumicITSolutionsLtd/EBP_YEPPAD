package com.serviceprovider.serviceprovider.controller;

import com.serviceprovider.serviceprovider.dto.ServiceProviderDTO;
import com.serviceprovider.serviceprovider.entity.ServiceProvider;
import com.serviceprovider.serviceprovider.model.Status;
import com.serviceprovider.serviceprovider.service.ServiceProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/serviceProvider")
public class ServiceProviderController {

    @Autowired
    private ServiceProviderService providerService;

    @GetMapping
    public Page<ServiceProvider> getAll(@RequestParam(required = false)Status status,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "0") int size){
        return  providerService.getAllServiceProviders(status,page, size);
    }

    @GetMapping("/{id}")
    public ServiceProvider getById(@PathVariable UUID id){return providerService.getById(id);}

}
