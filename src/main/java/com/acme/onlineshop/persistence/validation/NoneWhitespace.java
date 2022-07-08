package com.acme.onlineshop.persistence.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * The annotated element must not hold any whitespace character.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoneWhitespaceValidator.class)
@Documented
public @interface NoneWhitespace {

    String message() default "Not a NONE white space string";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
