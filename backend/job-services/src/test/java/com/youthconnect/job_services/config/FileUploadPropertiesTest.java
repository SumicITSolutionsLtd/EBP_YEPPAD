package com.youthconnect.job_services.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for FileUploadProperties configuration
 *
 * Verifies that configuration properties are loaded correctly from application.yml
 */
@SpringBootTest
@ActiveProfiles("test")
class FileUploadPropertiesTest {

    @Autowired
    private FileUploadProperties uploadProperties;

    @Test
    void shouldLoadConfigurationProperties() {
        // Verify bean is loaded
        assertThat(uploadProperties).isNotNull();

        // Verify properties are bound correctly
        assertThat(uploadProperties.getMaxFileSize()).isEqualTo(10485760L);
        assertThat(uploadProperties.getDirectory()).isNotEmpty();
        assertThat(uploadProperties.getAllowedTypes()).isNotEmpty();
        assertThat(uploadProperties.getAllowedTypes()).contains("application/pdf");
    }

    @Test
    void shouldValidateAllowedTypes() {
        // Test allowed types
        assertThat(uploadProperties.isAllowedType("application/pdf")).isTrue();
        assertThat(uploadProperties.isAllowedType("application/msword")).isTrue();

        // Test disallowed types
        assertThat(uploadProperties.isAllowedType("image/png")).isFalse();
        assertThat(uploadProperties.isAllowedType("text/plain")).isFalse();
    }

    @Test
    void shouldCalculateFileSizeInMB() {
        double sizeMB = uploadProperties.getMaxFileSizeMB();
        assertThat(sizeMB).isEqualTo(10.0);
    }
}
