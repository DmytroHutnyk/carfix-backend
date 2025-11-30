package com.hutnyk.carfix.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for validating password against common passwords, validated by {@link NotCommonPasswordValidator}
 */
@Constraint(validatedBy = NotCommonPasswordValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NotCommonPassword {
    String message() default "This password is commonly used";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
