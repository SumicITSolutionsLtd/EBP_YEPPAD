// PhoneNumberValidator.java
package com.youthconnect.user_service.validation;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Validator for Ugandan phone numbers using Google's libphonenumber library
 * Supports multiple formats and validates against Uganda's numbering plan
 */
@Slf4j
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    private static final String UGANDA_REGION_CODE = "UG";

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        log.debug("Initializing Uganda phone number validator");
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return true; // Let @NotBlank handle empty validation
        }

        try {
            // Clean the phone number
            String cleanedNumber = phoneNumber.trim();

            // Parse the phone number
            Phonenumber.PhoneNumber parsedNumber = phoneNumberUtil.parse(cleanedNumber, UGANDA_REGION_CODE);

            // Validate the number
            boolean isValid = phoneNumberUtil.isValidNumber(parsedNumber) &&
                    phoneNumberUtil.getRegionCodeForNumber(parsedNumber).equals(UGANDA_REGION_CODE);

            if (!isValid) {
                log.debug("Invalid phone number format: {}", phoneNumber);
            }

            return isValid;

        } catch (Exception e) {
            log.debug("Phone number parsing failed for: {} - Error: {}", phoneNumber, e.getMessage());
            return false;
        }
    }
}