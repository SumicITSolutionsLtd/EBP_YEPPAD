package com.youthconnect.ussd_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main Application Class for USSD Service
 *
 * USSD Service Architecture:
 * ========================
 * - Handles USSD callbacks from Africa's Talking
 * - Manages user registration via USSD interface
 * - Provides menu-driven interface for opportunity discovery
 * - Integrates with backend services via Feign clients
 *
 * Key Features:
 * - Session management with automatic cleanup
 * - Circuit breaker pattern for resilience
 * - Comprehensive security validation
 * - Metrics and monitoring integration
 *
 * Technology Stack:
 * - Spring Boot 3.3.4
 * - Spring Cloud 2023.0.3
 * - OpenFeign for inter-service communication
 * - Resilience4j for fault tolerance
 * - Micrometer for observability
 *
 * @author YouthConnect Uganda Development Team
 * @version 1.0.0
 * @since 2025-01-29
 */
@SpringBootApplication
@EnableDiscoveryClient        // Register with Eureka service registry
@EnableFeignClients           // CRITICAL: Enable Feign for REST clients
@EnableScheduling             // Enable scheduled session cleanup
@EnableRetry                  // Enable retry mechanism for transient failures
@EnableCaching                // Enable caching for performance
public class UssdServiceApplication {

	/**
	 * Application entry point
	 *
	 * Startup Process:
	 * 1. Load application configuration
	 * 2. Initialize Spring context
	 * 3. Register with service discovery
	 * 4. Start web server on port 8004
	 * 5. Initialize Feign clients
	 * 6. Start session cleanup scheduler
	 *
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(UssdServiceApplication.class, args);
	}
}