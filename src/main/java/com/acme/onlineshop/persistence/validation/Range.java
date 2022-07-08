package com.acme.onlineshop.persistence.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RangeValidator.class)
public @interface Range {

    String message() default "One value is greater than the other, although it should not be";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
