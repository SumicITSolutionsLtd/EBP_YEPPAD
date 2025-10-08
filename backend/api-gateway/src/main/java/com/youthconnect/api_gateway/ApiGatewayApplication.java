package com.youthconnect.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the API Gateway microservice.
 *
 * With the reactive 'spring-cloud-starter-gateway', this class requires no special annotations
 * other than @SpringBootApplication. All routing and filter logic is now handled by the
 * configuration in application.yml and any defined Configuration Beans.
 */
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}