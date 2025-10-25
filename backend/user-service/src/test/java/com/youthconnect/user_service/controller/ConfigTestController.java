package com.youthconnect.user_service.controller;

import com.youthconnect.user_service.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class ConfigTestController {

    private final ApplicationProperties properties;

    @GetMapping("/test")
    public Map<String, Object> testConfig() {
        return Map.of(
                "appName", properties.getName(),
                "environment", properties.getEnvironment(),
                "uploadDir", properties.getUpload().getUploadDirectory(),
                "maxFileSize", properties.getUpload().getMaxFileSize(),
                "allowedExtensions", properties.getUpload().getAllowedExtensions()
        );
    }
}