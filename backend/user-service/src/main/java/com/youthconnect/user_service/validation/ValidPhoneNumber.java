// ValidPhoneNumber.java (Annotation)
package com.youthconnect.user_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for Ugandan phone numbers
 * Validates phone numbers according to Uganda's numbering plan
 */
@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {
    String message() default "Invalid phone number format for Uganda (+256XXXXXXXXX or 07XXXXXXXX)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}