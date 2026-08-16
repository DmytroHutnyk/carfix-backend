package com.hutnyk.carfix.util;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;

import java.time.LocalDate;
import java.time.LocalTime;

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
    public static String notBlank(String value, String fieldName){
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
        notBlank(email, fieldName);

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
        notBlank(phoneNumber, fieldName);

        if (!phoneNumber.matches("^[0-9]{5,15}$")) {
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_PHONE_FORMAT, fieldName, phoneNumber);
        }
        return phoneNumber;
    }

    /**
     * Validates an international phone number: a leading '+' followed by 5–15 digits
     * (branch phones are stored as one international string, e.g. +48221234567).
     */
    public static String validateInternationalPhoneNumber(String phoneNumber, String fieldName) {
        notBlank(phoneNumber, fieldName);

        if (!phoneNumber.matches("^\\+[0-9]{5,15}$")) {
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_PHONE_FORMAT, fieldName, phoneNumber);
        }
        return phoneNumber;
    }

    /**
     * Validates that a date is today or in the future.
     *
     * @param date the date to validate
     * @param fieldName the name of the field being validated
     * @return the validated date
     * @throws DomainObjectValidationException if date is null or in the past
     */
    public static LocalDate futureOrPresent(LocalDate date, String fieldName) {
        notNull(date, fieldName);

        if (date.isBefore(LocalDate.now())) {
            throw new DomainObjectValidationException(ValidationErrorType.DATE_IN_PAST, fieldName, date);
        }
        return date;
    }

    /**
     * Validates that a time range's end is strictly after its start.
     *
     * @param start the lower bound of the range
     * @param end the upper bound of the range
     * @param fieldName the name of the field being validated
     * @throws DomainObjectValidationException if either bound is null or end is not after start
     */
    public static void validTimeRange(LocalTime start, LocalTime end, String fieldName) {
        notNull(start, "startTime");
        notNull(end, "endTime");

        if (!end.isAfter(start)) {
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_TIME_RANGE, fieldName);
        }
    }

}
