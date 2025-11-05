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
 * Job Service Application - Main Entry Point
 *
 * Microservice responsible for managing job postings and applications in the
 * Youth Entrepreneurship Booster Platform.
 *
 * KEY FEATURES:
 * - Job posting and management (CRUD operations)
 * - Job application submission and tracking
 * - AI-powered job recommendations
 * - Advanced search and filtering
 * - Automatic job expiration handling
 * - Real-time notifications integration
 * - Redis caching for performance
 * - Service discovery with Eureka
 *
 * TECHNOLOGY STACK:
 * - Spring Boot 3.2.0
 * - PostgreSQL 15 (primary database)
 * - Redis (caching layer)
 * - Spring Cloud (microservices)
 * - Feign (inter-service communication)
 * - JWT (authentication - validated by API Gateway)
 *
 * ARCHITECTURE:
 * - Clean architecture with layered separation
 * - DTOs for API contracts
 * - Entities for database mapping
 * - Mappers for transformations
 * - Services for business logic
 * - Repositories for data access
 *
 * @author Douglas Kings Kato
 * @version 3.0.0 (UUID Migration Complete)
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient      // Register with Eureka for service discovery
@EnableFeignClients         // Enable Feign for inter-service REST calls
@EnableCaching             // Enable Spring Cache abstraction (Caffeine + Redis)
@EnableAsync               // Enable async processing for non-blocking operations
@EnableScheduling          // Enable scheduled tasks (job expiration, reminders)
@EnableJpaAuditing        // Enable automatic created_at/updated_at timestamps
public class JobServicesApplication {

	/**
	 * Main application entry point
	 *
	 * Starts the Spring Boot application with all configured components:
	 * - Embedded Tomcat server (port 8000)
	 * - PostgreSQL connection pooling
	 * - Redis cache connections
	 * - Eureka service registration
	 * - Scheduled task executors
	 *
	 * @param args Command-line arguments (not used)
	 */
	public static void main(String[] args) {
		SpringApplication.run(JobServicesApplication.class, args);
	}
}