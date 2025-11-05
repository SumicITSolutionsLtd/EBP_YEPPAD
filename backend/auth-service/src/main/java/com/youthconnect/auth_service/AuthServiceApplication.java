package com.youthconnect.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ============================================================================
 * Main Application Class for Auth Service
 * ============================================================================
 *
 * This is the standalone authentication and authorization microservice for the
 * Youth Connect Uganda Platform. It handles all authentication flows including:
 *
 * FEATURES:
 * - Web-based user login/registration (email/phone + password)
 * - USSD-based authentication (phone-only, no password)
 * - JWT token generation and validation
 * - Refresh token management (7-day expiry)
 * - Password reset workflows (15-minute token expiry)
 * - Token blacklisting for logout (Redis-based)
 * - Account lockout after failed attempts
 * - Multi-factor authentication support
 *
 * ARCHITECTURE PATTERN: Microservices
 * - Communication: REST + Feign Clients for inter-service calls
 * - Service Discovery: Netflix Eureka
 * - Security: JWT-based stateless authentication
 * - Caching: Redis for token storage and blacklisting
 * - Database: PostgreSQL with Flyway migrations
 * - Resilience: Circuit Breaker + Retry patterns
 *
 * DEPENDENCIES:
 * - User Service: User data retrieval and registration
 * - Notification Service: Email/SMS notifications
 * - Service Registry: Eureka for service discovery
 * - Redis: Token blacklisting and caching
 * - PostgreSQL: User credentials and token persistence
 *
 * ENABLED FEATURES:
 * @EnableDiscoveryClient      - Registers with Eureka service registry
 * @EnableFeignClients         - Enables declarative REST clients
 * @EnableJpaAuditing          - Automatic createdAt/updatedAt timestamps
 * @EnableAsync                - Asynchronous method execution
 * @EnableScheduling           - Scheduled tasks (token cleanup)
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-01
 *
 * CHANGE LOG:
 * - v1.0.0 (2025-01-01): Initial production release
 * - v0.9.0 (2024-12-15): Beta release with core features
 * - v0.5.0 (2024-11-01): Alpha release for testing
 */
@SpringBootApplication
@EnableDiscoveryClient      // Enable Eureka service registration
@EnableFeignClients         // Enable Feign clients for inter-service communication
@EnableJpaAuditing          // Enable automatic auditing (createdAt, updatedAt)
@EnableAsync                // Enable asynchronous method execution
@EnableScheduling           // Enable scheduled tasks (e.g., token cleanup)
public class AuthServiceApplication {

	/**
	 * Main entry point for the Auth Service application.
	 *
	 * Startup Sequence:
	 * 1. Initialize Spring Boot application context
	 * 2. Load application properties (application.yml)
	 * 3. Connect to PostgreSQL database
	 * 4. Connect to Redis cache
	 * 5. Register with Eureka service registry
	 * 6. Initialize Feign clients (User Service, Notification Service)
	 * 7. Start embedded Tomcat server on configured port
	 * 8. Enable health check endpoints
	 * 9. Start scheduled tasks (token cleanup)
	 *
	 * @param args Command line arguments (not used)
	 */
	public static void main(String[] args) {
		// Start Spring Boot application
		SpringApplication.run(AuthServiceApplication.class, args);

		// Display startup banner with service information
		printStartupBanner();
	}

	/**
	 * Display formatted startup banner with service information.
	 * Provides quick reference to important endpoints and configuration.
	 */
	private static void printStartupBanner() {
		System.out.println("""
            
            ╔════════════════════════════════════════════════════════════╗
            ║                                                            ║
            ║     Youth Connect Uganda - Auth Service                   ║
            ║     Version: 1.0.0                                         ║
            ║     Environment: Development                               ║
            ║                                                            ║
            ║     Service successfully started!                          ║
            ║                                                            ║
            ║     📚 API Documentation:                                  ║
            ║        http://localhost:8083/swagger-ui.html               ║
            ║                                                            ║
            ║     ✅ Health Check:                                       ║
            ║        http://localhost:8083/api/auth/health               ║
            ║                                                            ║
            ║     📊 Metrics:                                            ║
            ║        http://localhost:8083/actuator/health               ║
            ║        http://localhost:8083/actuator/prometheus           ║
            ║                                                            ║
            ║     🔐 Authentication Endpoints:                           ║
            ║        POST /api/auth/login                                ║
            ║        POST /api/auth/register                             ║
            ║        POST /api/auth/ussd/login                           ║
            ║        POST /api/auth/refresh                              ║
            ║        POST /api/auth/logout                               ║
            ║        GET  /api/auth/validate                             ║
            ║                                                            ║
            ║     🔑 Password Reset:                                     ║
            ║        POST /api/auth/password/forgot                      ║
            ║        GET  /api/auth/password/validate-reset-token        ║
            ║        POST /api/auth/password/reset                       ║
            ║                                                            ║
            ║     Built with ❤️ by Douglas Kings Kato            ║
            ║                                                            ║
            ╚════════════════════════════════════════════════════════════╝
            
            """);
	}
}