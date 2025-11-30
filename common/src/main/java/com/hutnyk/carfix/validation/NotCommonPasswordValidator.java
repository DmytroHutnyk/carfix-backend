package com.hutnyk.carfix.validation;

import com.hutnyk.carfix.PasswordsReader;
import com.hutnyk.carfix.util.Validator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;


/**
 * Validation class for {@link NotCommonPassword} annotation. Validates password <code>String</code>
 * against common passwords uploaded from file.
 * <p>
 * File location specified by <code>NOT_ALLOWED_PASSWORDS_RECOURSE</code> field.
 * <p>
 * {@link PasswordsReader} populates <code>NOT_ALLOWED_PASSWORDS</code> field.
 */
public class NotCommonPasswordValidator implements ConstraintValidator<NotCommonPassword, String> {
    private final String NOT_ALLOWED_PASSWORDS_RECOURSE = "/passwords.txt";
    private final Set<String> NOT_ALLOWED_PASSWORDS = PasswordsReader.readPasswords(NOT_ALLOWED_PASSWORDS_RECOURSE);


    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext){
        Validator.notBlank(value, "password");

        return !NOT_ALLOWED_PASSWORDS.contains(value);
    }

}
