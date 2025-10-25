package com.youthconnect.mentor_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * ============================================================================
 * SWAGGER/OPENAPI CONFIGURATION
 * ============================================================================
 *
 * Configures Swagger UI and OpenAPI documentation for the mentor service.
 * Provides interactive API documentation accessible at /swagger-ui.html
 *
 * DOCUMENTATION FEATURES:
 * - Complete API endpoint documentation
 * - Request/response schemas
 * - Authentication requirements
 * - Try-it-out functionality
 * - Code generation support
 *
 * SWAGGER UI ACCESS:
 * - Development: http://localhost:8090/swagger-ui.html
 * - Staging: https://staging-api.platform.ug/mentor-service/swagger-ui.html
 * - Production: https://api.platform.ug/mentor-service/swagger-ui.html
 *
 * OPENAPI SPECIFICATION:
 * - JSON: /v3/api-docs
 * - YAML: /v3/api-docs.yaml
 *
 * SECURITY:
 * - JWT Bearer token authentication documented
 * - All protected endpoints marked with security requirement
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 * @since 2025-01-21
 * ============================================================================
 */
@Configuration
@Slf4j
public class SwaggerConfig {

    @Value("${spring.application.name:mentor-service}")
    private String applicationName;

    @Value("${server.port:8090}")
    private String serverPort;

    /**
     * OpenAPI Configuration Bean
     * Defines API metadata and security schemes
     *
     * METADATA:
     * - Title: API name
     * - Description: API purpose and features
     * - Version: Current API version
     * - Contact: Development team contact info
     * - License: API license information
     *
     * SECURITY:
     * - Bearer JWT: JWT token authentication
     * - Header: Authorization: Bearer {token}
     *
     * @return Configured OpenAPI instance
     */
    @Bean
    public OpenAPI mentorServiceOpenAPI() {
        log.info("Configuring OpenAPI documentation for {}", applicationName);

        return new OpenAPI()
                // API Information
                .info(new Info()
                        .title("Mentor Service API")
                        .description(
                                "Mentorship matching, session scheduling, and mentor management API.\n\n" +
                                        "## Features\n" +
                                        "- **Mentor Profile Management**: Create and manage mentor profiles with expertise areas\n" +
                                        "- **Session Scheduling**: Book, manage, and track mentorship sessions\n" +
                                        "- **Availability Management**: Configure mentor availability schedules\n" +
                                        "- **Review System**: Rate and review mentorship sessions\n" +
                                        "- **Matching Algorithm**: AI-powered mentor-mentee matching\n" +
                                        "- **Session Reminders**: Automated reminder notifications\n\n" +
                                        "## Authentication\n" +
                                        "All endpoints require JWT authentication. Include your JWT token in the Authorization header:\n" +
                                        "```\n" +
                                        "Authorization: Bearer {your-jwt-token}\n" +
                                        "```\n\n" +
                                        "## Rate Limiting\n" +
                                        "API requests are rate limited to 100 requests per minute per user."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Youth Connect Development Team")
                                .email("dev@youthconnect.ug")
                                .url("https://youthconnect.ug")
                        )
                        .license(new License()
                                .name("Proprietary")
                                .url("https://youthconnect.ug/license")
                        )
                )

                // API Servers
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("https://staging-api.platform.ug/mentor-service")
                                .description("Staging Environment"),
                        new Server()
                                .url("https://api.platform.ug/mentor-service")
                                .description("Production Environment")
                ))

                // Security Schemes
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT authentication token from auth-service")
                        )
                )

                // Global security requirement
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}