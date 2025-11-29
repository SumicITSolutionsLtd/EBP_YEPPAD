package com.youthconnect.common.validation;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) return true;

        try {
            // Default to Uganda (UG) if country code is missing, but handles +256 correctly
            Phonenumber.PhoneNumber number = phoneNumberUtil.parse(phoneNumber, "UG");
            return phoneNumberUtil.isValidNumber(number);
        } catch (Exception e) {
            log.warn("Phone validation error: {}", e.getMessage());
            return false;
        }
    }
}