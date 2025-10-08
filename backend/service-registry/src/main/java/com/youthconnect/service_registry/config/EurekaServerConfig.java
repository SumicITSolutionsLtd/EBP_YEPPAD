package com.youthconnect.service_registry.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Eureka Server Configuration.
 *
 * <p>This configuration class customizes the Eureka Server behavior and UI.
 * It configures:
 * <ul>
 *   <li>Custom home page redirect</li>
 *   <li>Static resource handling</li>
 *   <li>View controllers for additional custom pages</li>
 * </ul>
 *
 * <h2>Configuration Details:</h2>
 * <pre>
 * Default Eureka UI: /eureka
 * Custom Home Page: / → /index.html
 * Static Resources: /static/** (served by Spring Boot automatically)
 * </pre>
 *
 * <h2>URL Mappings:</h2>
 * <table border="1">
 *   <tr>
 *     <th>URL</th>
 *     <th>Description</th>
 *   </tr>
 *   <tr>
 *     <td>/</td>
 *     <td>Custom landing page with YouthConnect branding</td>
 *   </tr>
 *   <tr>
 *     <td>/eureka</td>
 *     <td>Standard Eureka dashboard</td>
 *   </tr>
 *   <tr>
 *     <td>/actuator/*</td>
 *     <td>Spring Boot Actuator endpoints</td>
 *   </tr>
 * </table>
 *
 * @author YouthConnect Uganda
 * @version 2.0.0
 * @since 2025-01-29
 */
@Configuration
public class EurekaServerConfig implements WebMvcConfigurer {

    /**
     * Configures custom view controllers.
     *
     * <p>This method overrides the default behavior to:
     * <ul>
     *   <li>Redirect root ("/") to a custom landing page (index.html)</li>
     *   <li>Allow extension for additional static/custom pages</li>
     * </ul>
     *
     * @param registry ViewControllerRegistry used to map URLs to views
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirect root URL to custom home page
        registry.addViewController("/").setViewName("forward:/index.html");

        // Example: add a custom static page mapping
        // registry.addViewController("/custom").setViewName("forward:/custom.html");
    }

    /*
     * Note:
     * Static resources are automatically served by Spring Boot from:
     * - /static/
     * - /public/
     * - /resources/
     * - /META-INF/resources/
     *
     * No additional configuration is needed unless you want to override
     * Spring Boot's default WebMvcAutoConfiguration.
     */
}
