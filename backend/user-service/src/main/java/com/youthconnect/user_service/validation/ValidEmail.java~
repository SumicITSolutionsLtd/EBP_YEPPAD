// ValidEmail.java (Enhanced email validation)
package com.youthconnect.user_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Enhanced email validation for Uganda context
 * Includes domain validation and common email patterns
 */
@Documented
@Constraint(validatedBy = EmailValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmail {
    String message() default "Invalid email address format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}