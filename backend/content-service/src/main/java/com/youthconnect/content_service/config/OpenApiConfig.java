package com.youthconnect.content_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 * API Documentation available at: /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI contentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Content Service API")
                        .description("Learning Modules & Community Content Management")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Douglas Kings Kato")
                                .email("damienpapers3@gmail.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8084")
                                .description("Development Server"),
                        new Server()
                                .url("http://localhost:8080/content-service")
                                .description("API Gateway")
                ));
    }
}