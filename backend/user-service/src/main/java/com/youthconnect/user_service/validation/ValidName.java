// ValidName.java (Annotation for Ugandan names)
package com.youthconnect.user_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for names in Uganda context
 * Allows for local naming conventions and special characters
 */
@Documented
@Constraint(validatedBy = UgandanNameValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidName {
    String message() default "Invalid name format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int minLength() default 2;
    int maxLength() default 50;
}