package com.youthconnect.mentor_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ============================================================================
 * MENTOR SERVICE - MAIN APPLICATION CLASS
 * ============================================================================
 *
 * Service: Mentorship matching, session scheduling, and mentor management
 * Port: 8085
 * Dependencies: user-service, notification-service, service-registry
 *
 * KEY RESPONSIBILITIES:
 * 1. Mentor profile management and verification
 * 2. Mentorship session scheduling and tracking
 * 3. Mentor-mentee matching algorithm
 * 4. Review and rating system
 * 5. Availability calendar management
 * 6. Session reminder notifications
 *
 * ARCHITECTURE FEATURES:
 * - Microservice architecture with Spring Cloud
 * - Service discovery with Eureka
 * - Inter-service communication via Feign
 * - Caching with Caffeine for performance
 * - Async processing for background tasks
 * - Scheduled jobs for reminders and cleanup
 * - Circuit breaker pattern for resilience
 * - Comprehensive metrics and monitoring
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-21
 * ============================================================================
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.youthconnect.mentor_service.client")
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties
public class MentorServiceApplication {

	/**
	 * Main entry point for the Mentor Service application.
	 *
	 * Initializes:
	 * - Spring Boot application context
	 * - Service registration with Eureka
	 * - Database connections and migrations
	 * - Cache managers
	 * - Scheduled task executors
	 * - Feign clients for inter-service communication
	 *
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(MentorServiceApplication.class, args);
	}
}