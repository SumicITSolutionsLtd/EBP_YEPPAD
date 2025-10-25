package com.youthconnect.user_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Swagger/OpenAPI Configuration for Entrepreneurship Booster Platform Uganda User Service
 *
 * Provides comprehensive API documentation using OpenAPI 3.0 specification
 * Access documentation at: http://localhost:8081/swagger-ui.html
 *
 * @author Douglas Kings Kato
 * @version 1.0.0
 */
@Configuration
public class SwaggerConfig {

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.name:Entrepreneurship Booster Platform Uganda User Service}")
    private String appName;

    @Value("${server.port:8081}")
    private String serverPort;

    @Value("${app.environment:development}")
    private String environment;

    /**
     * Main OpenAPI configuration bean
     */
    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .servers(createServerList())
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(createComponents())
                .tags(createTags());
    }

    /**
     * Creates detailed API information
     */
    private Info createApiInfo() {
        return new Info()
                .title(appName + " - API Documentation")
                .description(createApiDescription())
                .version(appVersion)
                .contact(createContactInfo())
                .license(createLicenseInfo());
    }

    /**
     * Creates API description
     */
    private String createApiDescription() {
        return """
                ## Entrepreneurship Booster Platform Uganda User Service API
                
                ### Core Features:
                - Multi-role User Registration (Youth, Mentors, NGOs, Funders, Service Providers, Admins)
                - JWT Authentication & Authorization
                - USSD Integration for mobile registration
                - Comprehensive Profile Management
                - Role-based Access Control
                
                ### Environment: """ + environment.toUpperCase();
    }

    /**
     * Creates contact information
     */
    private Contact createContactInfo() {
        return new Contact()
                .name("Entrepreneurship Booster Platform Uganda Development Team")
                .email("dev@entrepreneurshipboosterplatform.ug")
                .url("https://entrepreneurshipboosterplatform.ug");
    }

    /**
     * Creates licensing information
     */
    private License createLicenseInfo() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * Creates server configurations
     */
    private List<Server> createServerList() {
        return Arrays.asList(
                new Server()
                        .url("http://localhost:" + serverPort)
                        .description("Local Development"),
                new Server()
                        .url("https://api.entrepreneurshipboosterplatform.ug")
                        .description("Production")
        );
    }

    /**
     * Creates security components
     */
    private Components createComponents() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token from /api/auth/login"));
    }

    /**
     * Creates API tags
     */
    private List<Tag> createTags() {
        return Arrays.asList(
                new Tag()
                        .name("Authentication")
                        .description("User authentication endpoints"),
                new Tag()
                        .name("User Management")
                        .description("User profile management"),
                new Tag()
                        .name("USSD Integration")
                        .description("Mobile USSD endpoints"),
                new Tag()
                        .name("System Health")
                        .description("Health check endpoints")
        );
    }
}