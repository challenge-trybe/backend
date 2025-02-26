package com.trybe.moduleapi.annotation;

import com.trybe.moduleapi.annotation.validator.NonEmptyListValidator;
import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NonEmptyListValidator.class)
public @interface NotEmptyList {
    String message() default "List must not be empty";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};
}