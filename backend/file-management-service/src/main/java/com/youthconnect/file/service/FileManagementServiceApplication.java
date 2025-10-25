package com.youthconnect.file.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * File Management Service Application
 *
 * Handles all file operations for the Youth Entrepreneurship Platform:
 * - Profile picture uploads with image optimization
 * - Audio file management for multi-language learning modules
 * - Document storage for applications and certifications
 * - Secure file access with validation
 *
 * CRITICAL FEATURES:
 * - Multi-format support (images, audio, documents)
 * - File validation and virus scanning
 * - Storage abstraction (local/S3/MinIO)
 * - Async processing for large files
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
public class FileManagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileManagementServiceApplication.class, args);
	}
}