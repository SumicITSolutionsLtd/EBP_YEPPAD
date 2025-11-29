package com.youthconnect.job_services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * JOB SERVICE APPLICATION - MAIN ENTRY POINT (FIXED v3.0.5)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Microservice for managing job postings and applications.
 *
 * FIXES APPLIED:
 * ❌ REMOVED: @EnableConfigurationProperties(FileUploadProperties.class)
 *    - FileUploadProperties now has @Component annotation
 *    - This prevents duplicate bean creation
 *
 * KEY FEATURES:
 * - Job posting and management (CRUD)
 * - Job application tracking
 * - AI-powered recommendations
 * - File upload management
 * - Advanced search and filtering
 * - Redis caching
 * - Service discovery (Eureka)
 *
 * TECHNOLOGY STACK:
 * - Spring Boot 3.2.0
 * - PostgreSQL 15+
 * - Redis 7+
 * - Spring Cloud 2023.0.0
 * - JWT Authentication
 *
 * @author Douglas Kings Kato
 * @version 3.0.5 (Bean Conflict Fix)
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient      // Eureka service discovery
@EnableFeignClients         // Feign client for inter-service calls
@EnableCaching             // Caffeine + Redis caching
@EnableAsync               // Async processing
@EnableScheduling          // Scheduled tasks
@EnableJpaAuditing        // Automatic timestamps
public class JobServicesApplication {

	/**
	 * Application entry point
	 *
	 * Starts Spring Boot application with:
	 * - Embedded Tomcat (port 8000)
	 * - PostgreSQL connection pool
	 * - Redis cache
	 * - Eureka registration
	 * - File upload service
	 *
	 * @param args Command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(JobServicesApplication.class, args);
	}
}