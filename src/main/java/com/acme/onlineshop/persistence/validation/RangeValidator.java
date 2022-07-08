package com.acme.onlineshop.persistence.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class RangeValidator implements ConstraintValidator<Range, RangeDefinition> {

    @Override
    public void initialize(Range constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(RangeDefinition instance, ConstraintValidatorContext context) {
        return instance.smallerValue() < instance.greaterValue();
    }
}
