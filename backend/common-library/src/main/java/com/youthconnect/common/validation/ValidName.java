package com.youthconnect.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UgandanNameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidName {
    String message() default "Invalid name format";
    int minLength() default 2;
    int maxLength() default 50;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}