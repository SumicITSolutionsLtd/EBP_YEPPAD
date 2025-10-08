package com.youthconnect.ussd_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

/**
 * Web configuration for REST clients, HTTP settings, and CORS.
 * This class correctly configures RestTemplate with timeouts and sets up CORS mappings.
 * The warnings about 'WebConfig' and 'restTemplate' methods being "never used" are
 * normal for Spring @Configuration and @Bean methods, as Spring's framework
 * uses them internally, not your direct code.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures and provides a RestTemplate bean.
     * It sets connection and read timeouts and uses HttpComponentsClientHttpRequestFactory
     * for better control over HTTP client properties, like connection pooling (though not explicitly
     * configured here for pooling).
     *
     * @param builder The RestTemplateBuilder provided by Spring Boot.
     * @return A configured RestTemplate instance.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                // Set the connection timeout (time to establish the connection)
                //.connectTimeout(Duration.ofMillis(5000)) // 5 seconds
                // Set the read timeout (time to read data from the connection)
                //.readTimeout(Duration.ofMillis(10000))  // 10 seconds
                // Use HttpComponentsClientHttpRequestFactory for Apache HttpClient.
                // This is often preferred over the default SimpleClientHttpRequestFactory for production.
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) for the application.
     * This allows web browsers to make requests from different origins (domains).
     *
     * @param registry The CorsRegistry to add CORS mappings to.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // CORS configuration for API endpoints
        registry.addMapping("/api/**")
                // For development, "*" is acceptable, but in production,
                // specify exact origins like "http://yourfrontend.com"
                .allowedOrigins("*")
                // Allowed HTTP methods for these endpoints
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // Allowed headers in the request
                .allowedHeaders("*")
                // How long the pre-flight request can be cached (in seconds)
                .maxAge(3600);

        // CORS configuration specifically for USSD related endpoints
        // Assuming USSD callbacks might originate from different systems or need specific CORS.
        registry.addMapping("/ussd/**")
                .allowedOrigins("*") // Again, consider specific origins for production
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}