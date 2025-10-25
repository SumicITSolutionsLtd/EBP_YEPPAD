package com.youthconnect.api_gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Youth Connect Uganda - API Gateway Application
 *
 * Main entry point for the API Gateway microservice.
 *
 * The API Gateway serves as the single entry point for all client requests,
 * providing:
 * - Intelligent routing to backend microservices
 * - Rate limiting to prevent abuse
 * - CORS configuration for web/mobile clients
 * - Circuit breaker for resilience
 * - Security headers injection
 * - Request/response logging
 * - Global exception handling
 *
 * All client applications (web, mobile, USSD) communicate through this gateway,
 * which then routes requests to the appropriate backend service based on
 * URL patterns defined in application.yml.
 *
 * @author Youth Connect Development Team
 * @version 1.0.0
 * @since 2025-01-20
 */
@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ApiGatewayApplication.class, args);
		logApplicationStartup(context.getEnvironment());
	}

	/**
	 * Log application startup information
	 * Displays all important URLs and configuration details
	 */
	private static void logApplicationStartup(Environment env) {
		String protocol = "http";
		if (env.getProperty("server.ssl.key-store") != null) {
			protocol = "https";
		}

		String serverPort = env.getProperty("server.port", "8080");
		String contextPath = env.getProperty("server.servlet.context-path", "/");
		String hostAddress = "localhost";

		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.warn("Unable to determine host address");
		}

		String eurekaUrl = env.getProperty("eureka.client.service-url.defaultZone", "http://localhost:8761/eureka");

		log.info("\n----------------------------------------------------------\n" +
						"🚀 API Gateway Started Successfully!\n" +
						"----------------------------------------------------------\n" +
						"📌 Application: {}\n" +
						"🌐 Local URL: {}://localhost:{}{}\n" +
						"🌍 External URL: {}://{}:{}{}\n" +
						"📊 Eureka Server: {}\n" +
						"🔧 Profile(s): {}\n" +
						"🎯 Actuator: {}://localhost:{}/actuator\n" +
						"📈 Metrics: {}://localhost:{}/actuator/prometheus\n" +
						"💚 Health: {}://localhost:{}/actuator/health\n" +
						"----------------------------------------------------------",
				env.getProperty("spring.application.name", "api-gateway"),
				protocol, serverPort, contextPath,
				protocol, hostAddress, serverPort, contextPath,
				eurekaUrl,
				env.getActiveProfiles().length > 0 ? String.join(", ", env.getActiveProfiles()) : "default",
				protocol, serverPort,
				protocol, serverPort,
				protocol, serverPort);
	}
}