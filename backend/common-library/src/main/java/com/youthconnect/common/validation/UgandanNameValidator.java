package com.youthconnect.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validator for names supporting Ugandan naming conventions.
 * Allows letters, spaces, apostrophes (for names like O'Brien), hyphens, and dots.
 */
public class UgandanNameValidator implements ConstraintValidator<ValidName, String> {

    // Regex: Allows letters, spaces, apostrophes, hyphens, and dots.
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-Z\\s'.-]+$", Pattern.UNICODE_CHARACTER_CLASS);

    private int minLength;
    private int maxLength;

    @Override
    public void initialize(ValidName constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        // Null/Empty is handled by @NotBlank usually, return true here to avoid conflict
        if (name == null || name.trim().isEmpty()) {
            return true;
        }

        String trimmedName = name.trim();

        // Check Length
        if (trimmedName.length() < minLength || trimmedName.length() > maxLength) {
            return false;
        }

        // Check Regex
        return NAME_PATTERN.matcher(trimmedName).matches();
    }
}