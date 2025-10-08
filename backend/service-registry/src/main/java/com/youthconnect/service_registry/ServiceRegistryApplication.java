package com.youthconnect.service_registry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Service Registry Application - Eureka Server
 *
 * <p>This is the central service discovery server for the YouthConnect Uganda platform.
 * All microservices register with this Eureka server, enabling dynamic service discovery
 * and load balancing across the distributed system.
 *
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>Service registration and discovery</li>
 *   <li>Health monitoring of registered services</li>
 *   <li>Automatic deregistration of unhealthy services</li>
 *   <li>Load balancing support via Netflix Ribbon</li>
 *   <li>High availability with peer-to-peer replication (in cluster mode)</li>
 * </ul>
 *
 * <h2>Registered Services:</h2>
 * <ul>
 *   <li>API Gateway (8088) - Entry point for all client requests</li>
 *   <li>Auth Service (8080) - Authentication and authorization</li>
 *   <li>User Service (8082) - User profile management</li>
 *   <li>Opportunity Service (8083) - Opportunity management</li>
 *   <li>USSD Service (8004) - USSD interface</li>
 *   <li>Notification Service (8086) - SMS/Email notifications</li>
 *   <li>Content Service (8085) - Content management</li>
 *   <li>Mentor Service (8087) - Mentorship system</li>
 *   <li>AI Recommendation Service (8089) - ML-based recommendations</li>
 *   <li>Analytics Service (8090) - Analytics and reporting</li>
 * </ul>
 *
 * <h2>Architecture:</h2>
 * <pre>
 *     ┌─────────────────────────────────────────┐
 *     │     Service Registry (Eureka)           │
 *     │            Port: 8761                   │
 *     └──────────────┬──────────────────────────┘
 *                    │
 *         ┌──────────┴──────────┐
 *         │                     │
 *    [Register]            [Discover]
 *         │                     │
 *    ┌────▼─────┐         ┌────▼─────┐
 *    │ Services │         │ Clients  │
 *    │ (10+)    │         │ (API GW) │
 *    └──────────┘         └──────────┘
 * </pre>
 *
 * <h2>Production Deployment:</h2>
 * <ul>
 *   <li>Deploy in cluster mode (3+ nodes) for high availability</li>
 *   <li>Enable peer-to-peer replication for redundancy</li>
 *   <li>Implement health checks and auto-recovery</li>
 *   <li>Secure with Spring Security (Basic Auth/OAuth2)</li>
 *   <li>Monitor with Actuator endpoints and Prometheus</li>
 * </ul>
 *
 * @author YouthConnect Uganda Development Team
 * @version 2.0.0
 * @since 2025-01-29
 * @see <a href="https://cloud.spring.io/spring-cloud-netflix/">Spring Cloud Netflix</a>
 */
@Slf4j
@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {

	/**
	 * Main application entry point.
	 *
	 * <p>Starts the Eureka Server with the following initialization sequence:
	 * <ol>
	 *   <li>Load configuration from application.yml</li>
	 *   <li>Initialize Spring Boot context</li>
	 *   <li>Start Eureka Server dashboard (http://localhost:8761)</li>
	 *   <li>Begin accepting service registrations</li>
	 *   <li>Start health check scheduler</li>
	 *   <li>Enable peer replication (if configured)</li>
	 * </ol>
	 *
	 * @param args Command-line arguments (supports: --spring.profiles.active=prod)
	 */
	public static void main(String[] args) {
		// Start the Spring Boot application
		ConfigurableApplicationContext context = SpringApplication.run(ServiceRegistryApplication.class, args);

		// Log application startup information
		logApplicationStartup(context.getEnvironment());
	}

	/**
	 * Logs comprehensive startup information for monitoring and debugging.
	 *
	 * <p>Displays:
	 * <ul>
	 *   <li>Application name and version</li>
	 *   <li>Active Spring profiles</li>
	 *   <li>Server host and port</li>
	 *   <li>Eureka dashboard URL</li>
	 *   <li>Management/actuator endpoints</li>
	 * </ul>
	 *
	 * @param env Spring Environment containing application configuration
	 */
	private static void logApplicationStartup(Environment env) {
		String protocol = "http";
		if (env.getProperty("server.ssl.key-store") != null) {
			protocol = "https";
		}

		String serverPort = env.getProperty("server.port", "8761");
		String contextPath = env.getProperty("server.servlet.context-path", "/");
		String hostAddress = "localhost";

		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.warn("Unable to determine host address, using 'localhost'");
		}

		String activeProfiles = String.join(", ", env.getActiveProfiles());
		if (activeProfiles.isEmpty()) {
			activeProfiles = "default";
		}

		// Build startup banner
		String startupLog = String.format("""
            
            ================================================================================
            
                    🚀 Service Registry (Eureka Server) Started Successfully! 🚀
            
            ================================================================================
            
            Application:     %s
            Version:         %s
            Profile(s):      %s
            
            Access URLs:
            --------------------------------------------------------------------------------
            Eureka Dashboard:    %s://localhost:%s%s
            External:            %s://%s:%s%s
            Health Check:        %s://localhost:%s/actuator/health
            Metrics:             %s://localhost:%s/actuator/prometheus
            
            Management Endpoints:
            --------------------------------------------------------------------------------
            Actuator Base:       %s://localhost:%s/actuator
            
            Registered Services: (Will appear here after registration)
            --------------------------------------------------------------------------------
            View at: %s://localhost:%s%s
            
            Configuration:
            --------------------------------------------------------------------------------
            Java Version:        %s
            Timezone:            %s
            Encoding:            %s
            
            ================================================================================
            
            """,
				env.getProperty("spring.application.name", "service-registry"),
				env.getProperty("application.version", "2.0.0"),
				activeProfiles,
				protocol, serverPort, contextPath,
				protocol, hostAddress, serverPort, contextPath,
				protocol, serverPort,
				protocol, serverPort,
				protocol, serverPort,
				protocol, serverPort, contextPath,
				System.getProperty("java.version"),
				System.getProperty("user.timezone"),
				System.getProperty("file.encoding")
		);

		log.info(startupLog);
	}
}