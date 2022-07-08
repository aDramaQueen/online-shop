package com.acme.onlineshop.condition;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(SecurityActiveCondition.class)
public @interface SkipWhenSecurityDeactivated { }
