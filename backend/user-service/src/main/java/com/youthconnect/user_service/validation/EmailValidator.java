// EmailValidator.java
package com.youthconnect.user_service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * Enhanced email validator with domain checking
 */
@Slf4j
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        log.debug("Initializing enhanced email validator");
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Let @NotBlank handle empty validation
        }

        String trimmedEmail = email.trim().toLowerCase();

        // Basic pattern validation
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            log.debug("Email pattern validation failed for: {}", email);
            return false;
        }

        // Additional checks
        if (trimmedEmail.length() > 254) { // RFC 5321 limit
            log.debug("Email too long: {}", email);
            return false;
        }

        // Check for common issues
        if (trimmedEmail.startsWith(".") || trimmedEmail.endsWith(".") ||
                trimmedEmail.contains("..")) {
            log.debug("Email contains invalid dot placement: {}", email);
            return false;
        }

        return true;
    }
}