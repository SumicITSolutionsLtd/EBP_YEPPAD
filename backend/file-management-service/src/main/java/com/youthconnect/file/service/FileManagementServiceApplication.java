package com.youthconnect.file.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * File Management Service Application
 * Handles all file operations for the platform
 *
 * CRITICAL FEATURES:
 * - Profile picture upload and optimization
 * - Audio file management for learning modules
 * - Document storage for applications
 * - Multi-language audio support
 * - File security and validation
 */
@SpringBootApplication
@EnableEurekaClient
@EnableConfigurationProperties
@EnableAsync
public class FileManagementServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(FileManagementServiceApplication.class, args);
	}
}
