package com.acme.onlineshop.persistence.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NoneWhitespaceValidator implements ConstraintValidator<NoneWhitespace, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value.matches("\\S+");
    }
}
