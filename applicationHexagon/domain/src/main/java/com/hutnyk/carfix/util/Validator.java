package com.hutnyk.carfix.util;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;

import java.time.LocalDate;
import java.time.LocalTime;

public class Validator {
    public static <T> T notNull(T value, String fieldName){
        if(value == null){
            throw new DomainObjectValidationException(ValidationErrorType.NULL_VALUE, fieldName);
        }
        return value;
    }
    public static String notBlank(String value, String fieldName){
        notNull(value, fieldName);

        if(value.isBlank()){
            throw new DomainObjectValidationException(ValidationErrorType.EMPTY_STRING, fieldName, value);
        }
        return value;
    }
    public static String validateEmail(String email, String fieldName) {
        notBlank(email, fieldName);

        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_EMAIL_FORMAT, fieldName, email
            );
        }
        return email;
    }
    public static String validatePhoneNumber(String phoneNumber, String fieldName) {
        notBlank(phoneNumber, fieldName);

        if (!phoneNumber.matches("^[0-9]{5,15}$")) {
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_PHONE_FORMAT, fieldName, phoneNumber);
        }
        return phoneNumber;
    }
    public static String validateInternationalPhoneNumber(String phoneNumber, String fieldName) {
        notBlank(phoneNumber, fieldName);

        if (!phoneNumber.matches("^\\+[0-9]{5,15}$")) {
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_PHONE_FORMAT, fieldName, phoneNumber);
        }
        return phoneNumber;
    }
    public static LocalDate futureOrPresent(LocalDate date, String fieldName) {
        notNull(date, fieldName);

        if (date.isBefore(LocalDate.now())) {
            throw new DomainObjectValidationException(ValidationErrorType.DATE_IN_PAST, fieldName, date);
        }
        return date;
    }
    public static void validTimeRange(LocalTime start, LocalTime end, String fieldName) {
        notNull(start, "startTime");
        notNull(end, "endTime");

        if (!end.isAfter(start)) {
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_TIME_RANGE, fieldName);
        }
    }

}
