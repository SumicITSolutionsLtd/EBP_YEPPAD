package com.youthconnect.auth_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * SWAGGER OPENAPI CONFIGURATION
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Configures the interactive API documentation.
 * Includes JWT Bearer Authentication support.
 *
 * Added global SecurityRequirement so 'Authorize' button works.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearer-jwt";

        return new OpenAPI()
                // 1. API Information
                .info(new Info()
                        .title("Auth Service API - Entrepreneurship Booster Platform")
                        .version("1.0.0")
                        .description("Authentication and Authorization API for Youth Connect Uganda")
                        .contact(new Contact()
                                .name("Douglas Kings Kato - Entrepreneurship Booster Team")
                                .email("douglaskings2@gmail.com")
                                .url("https://ebp.ug"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))

                // 2. Define Security Scheme (JWT)
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("Enter your JWT token in the format: Bearer <token>")
                        ))

                // 3. APPLY SECURITY GLOBALLY
                // This ensures the 'Authorize' button actually attaches the token to requests
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}