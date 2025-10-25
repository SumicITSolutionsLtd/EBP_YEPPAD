// ValidPhoneNumber.java (Annotation)
package com.youthconnect.user_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {
    String message() default "Invalid Ugandan phone number (+256XXXXXXXXX or 07XXXXXXXX)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}