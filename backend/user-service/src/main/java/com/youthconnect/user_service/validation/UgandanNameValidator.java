// UgandanNameValidator.java
package com.youthconnect.user_service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * Validator for Ugandan names supporting local naming conventions
 * Allows for apostrophes, hyphens, and spaces in names
 */
@Slf4j
public class UgandanNameValidator implements ConstraintValidator<ValidName, String> {

    // Pattern allows letters, spaces, apostrophes, hyphens, and dots
    // Supports Ugandan naming conventions
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-Z\\s'.-]+$", Pattern.UNICODE_CHARACTER_CLASS);

    private int minLength;
    private int maxLength;

    @Override
    public void initialize(ValidName constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        log.debug("Initializing Ugandan name validator with length: {}-{}", minLength, maxLength);
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null || name.trim().isEmpty()) {
            return true; // Let @NotBlank handle empty validation
        }

        String trimmedName = name.trim();

        // Check length
        if (trimmedName.length() < minLength || trimmedName.length() > maxLength) {
            log.debug("Name length validation failed for: {} (length: {})", name, trimmedName.length());
            return false;
        }

        // Check pattern
        if (!NAME_PATTERN.matcher(trimmedName).matches()) {
            log.debug("Name pattern validation failed for: {}", name);
            return false;
        }

        // Additional checks for reasonable names
        // Don't allow names with only special characters
        if (trimmedName.replaceAll("[\\s'.-]", "").isEmpty()) {
            log.debug("Name contains only special characters: {}", name);
            return false;
        }

        // Don't allow consecutive spaces or special characters
        if (trimmedName.contains("  ") ||
                trimmedName.contains("--") ||
                trimmedName.contains("''") ||
                trimmedName.contains("..")) {
            log.debug("Name contains consecutive special characters: {}", name);
            return false;
        }

        return true;
    }
}