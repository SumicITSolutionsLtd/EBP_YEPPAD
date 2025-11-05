package com.youthconnect.job_services.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI Configuration
 *
 * Configures Swagger/OpenAPI documentation for the Job Service.
 * Access at: http://localhost:8000/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI jobServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Job Service API")
                        .description("Job posting and application management for Entrepreneurship Booster Platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Douglas Kings Kato")
                                .email("support@entrepreneurshipbooster.ug"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://entrepreneurshipbooster.ug/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8000")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.entrepreneurshipbooster.ug")
                                .description("Production Server")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT Bearer token")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}