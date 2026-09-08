package com.hutnyk.carfix.validation;

import com.hutnyk.carfix.PasswordsReader;
import com.hutnyk.carfix.util.Validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;


public class NotCommonPasswordValidator implements ConstraintValidator<NotCommonPassword, String> {
    private final String NOT_ALLOWED_PASSWORDS_RECOURSE = "/passwords.txt";
    private final Set<String> NOT_ALLOWED_PASSWORDS = PasswordsReader.readPasswords(NOT_ALLOWED_PASSWORDS_RECOURSE);


    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext){
        Validator.notBlank(value, "password");

        return !NOT_ALLOWED_PASSWORDS.contains(value);
    }

}
