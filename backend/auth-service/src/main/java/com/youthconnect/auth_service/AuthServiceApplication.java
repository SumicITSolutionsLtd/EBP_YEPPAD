package com.youthconnect.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Application Class for Auth Service
 *
 * This is the standalone authentication and authorization microservice for the
 * Youth Connect Uganda Platform. It handles all authentication flows including:
 * - Web-based user login/registration
 * - USSD-based authentication (phone-only)
 * - JWT token generation and validation
 * - Refresh token management
 * - Password reset workflows
 * - Token blacklisting for logout
 * - OAuth2 integration (future)
 *
 * Architecture Pattern: Microservices
 * Communication: REST + Feign Clients for inter-service calls
 * Service Discovery: Netflix Eureka
 * Security: JWT-based stateless authentication
 * Caching: Redis for token storage and blacklisting
 *
 * @author Youth Connect Uganda Development Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@SpringBootApplication
@EnableDiscoveryClient      // Enables Eureka service registration
@EnableFeignClients         // Enables declarative REST clients for user-service, notification-service
@EnableJpaAuditing          // Enables automatic auditing (createdAt, updatedAt)
@EnableAsync                // Enables asynchronous method execution
@EnableScheduling           // Enables scheduled tasks (e.g., token cleanup)
public class AuthServiceApplication {

	/**
	 * Main entry point for the Auth Service application
	 *
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);

		System.out.println("""
            ╔════════════════════════════════════════════════════════════╗
            ║                                                            ║
            ║     Kwetu-Hub Uganda - Auth Service                   ║
            ║     Version: 1.0.0                                         ║
            ║     Environment: Development                               ║
            ║                                                            ║
            ║     Service successfully started!                          ║
            ║     API Documentation: http://localhost:8082/swagger-ui    ║
            ║     Health Check: http://localhost:8082/actuator/health    ║
            ║                                                            ║
            ╚════════════════════════════════════════════════════════════╝
            """);
	}
}
