package com.trybe.moduleapi.annotation.validator;

import com.trybe.moduleapi.annotation.NotEmptyList;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class NonEmptyListValidator implements ConstraintValidator<NotEmptyList, List<?>> {
    @Override
    public boolean isValid(List<?> value, ConstraintValidatorContext context) {
        return value != null && !value.isEmpty();
    }

    @Override
    public void initialize(NotEmptyList constraintAnnotation) {
    }
}