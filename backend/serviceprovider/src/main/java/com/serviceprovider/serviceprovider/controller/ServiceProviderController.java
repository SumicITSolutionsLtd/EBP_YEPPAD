package com.serviceprovider.serviceprovider.controller;

import com.serviceprovider.serviceprovider.dto.ServiceProviderDTO;
import com.serviceprovider.serviceprovider.entity.ServiceProvider;
import com.serviceprovider.serviceprovider.model.Status;
import com.serviceprovider.serviceprovider.service.ServiceProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/{id}/update")
    public ServiceProvider getById(@PathVariable UUID id){return providerService.getById(id);}

    @PutMapping("/{id}")
    public ServiceProvider update(@PathVariable UUID id, @RequestBody ServiceProvider provider){
            return providerService.update(id, provider);
    }

    @PutMapping("/bulk/update")
    public Page<ServiceProvider> updateBulk(@RequestBody List<ServiceProvider> providerss,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceProvider> providersPage = new PageImpl<>(providerss, pageable, providerss.size());
        Page<ServiceProvider> updated = providerService.updateProviders(providersPage);
        return providerService.updateProviders(updated);
    }


}
