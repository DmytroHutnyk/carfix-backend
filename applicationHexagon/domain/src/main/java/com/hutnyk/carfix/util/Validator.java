package com.hutnyk.carfix.util;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;

public class Validator {

    /**
     * Validates that a value is not null.
     *
     * @param value the value to validate
     * @param fieldName the name of the field being validated (for error messages)
     * @return the validated value (not modified)
     * @throws DomainObjectValidationException if value is null
     */
    public static <T> T notNull(T value, String fieldName){
        if(value == null){
            throw new DomainObjectValidationException(ValidationErrorType.NULL_VALUE, fieldName);
        }
        return value;
    }

    /**
     * Validates that a string is not empty or blank.
     *
     * @param value the string to validate
     * @param fieldName the name of the field being validated
     * @return the validated string
     * @throws DomainObjectValidationException if value is null or blank
     */
    public static String notEmpty(String value, String fieldName){
        notNull(value, fieldName);

        if(value.isBlank()){
            throw new DomainObjectValidationException(ValidationErrorType.EMPTY_STRING, fieldName, value);
        }
        return value;
    }


    /**
     * Validates email format.
     */
    public static String validateEmail(String email, String fieldName) {
        notEmpty(email, fieldName);

        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_EMAIL_FORMAT, fieldName, email
            );
        }
        return email;
    }

    /**
     * Validates phone number format.
     */
    public static String validatePhoneNumber(String phoneNumber, String fieldName) {
        notEmpty(phoneNumber, fieldName);

        if (!phoneNumber.matches("^[0-9]{5,15}$")) {
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_PHONE_FORMAT, fieldName, phoneNumber);
        }
        return phoneNumber;
    }

}
