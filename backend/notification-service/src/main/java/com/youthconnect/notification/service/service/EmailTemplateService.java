package com.youthconnect.notification.service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Service for loading and processing HTML email templates.
 */
@Service
@Slf4j
public class EmailTemplateService {

    @Value("${spring.application.name:Entrepreneurship Booster Platform}")
    private String applicationName;

    @Value("${app.web-url:https://ebp.ug}")
    private String webUrl;

    /**
     * Load HTML template and replace placeholders.
     */
    public String loadTemplate(String templateName, Map<String, String> variables) {
        try {
            // Load template from resources
            ClassPathResource resource = new ClassPathResource(
                    "templates/" + templateName + ".html"
            );

            String template = StreamUtils.copyToString(
                    resource.getInputStream(),
                    StandardCharsets.UTF_8
            );

            // Replace global variables
            template = template.replace("{{APP_NAME}}", applicationName);
            template = template.replace("{{WEB_URL}}", webUrl);

            // Replace custom variables
            if (variables != null) {
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                    template = template.replace(
                            "{{" + entry.getKey() + "}}",
                            entry.getValue()
                    );
                }
            }

            return template;

        } catch (Exception e) {
            log.error("Failed to load template: {}", templateName, e);
            return getFallbackTemplate(variables);
        }
    }

    /**
     * Fallback template if file loading fails.
     */
    private String getFallbackTemplate(Map<String, String> variables) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2>%s</h2>
                <p>%s</p>
                <p>Visit: <a href="%s">%s</a></p>
            </body>
            </html>
            """,
                applicationName,
                variables.getOrDefault("MESSAGE", "You have a new notification."),
                webUrl,
                webUrl
        );
    }
}