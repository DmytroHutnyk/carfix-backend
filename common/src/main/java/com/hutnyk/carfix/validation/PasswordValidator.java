package com.hutnyk.carfix.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext constraintValidatorContext) {
        if(password == null || password.isBlank()){
            return false;
        }

        if(password.length() < 8 || password.length() > 20){
            return false;
        }


        boolean containsUpperCaseLetter = password.chars().anyMatch(Character::isUpperCase);
        boolean containsLowerCaseLetter = password.chars().anyMatch(Character::isLowerCase);
        boolean containsDigit = password.chars().anyMatch(Character::isDigit);
        boolean containsSpecialCharacter = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));


        return containsUpperCaseLetter &&
                containsLowerCaseLetter &&
                containsDigit &&
                containsSpecialCharacter;
    }
}
