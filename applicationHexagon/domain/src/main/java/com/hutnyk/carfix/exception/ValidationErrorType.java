package com.hutnyk.carfix.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@code ValidationErrorType} supplies validation exceptions with a default message and doubles as
 * their {@link ErrorCode}: every constant is a published code in the {@link ErrorCategory#VALIDATION}
 * category.
 */
@Getter
@AllArgsConstructor
public enum ValidationErrorType implements ErrorCode {
    NULL_VALUE("NULL_VALUE", "Value cannot be null"),
    EMPTY_STRING("EMPTY_STRING", "String cannot be empty or blank"),
    
    INVALID_EMAIL_FORMAT("INVALID_EMAIL_FORMAT", "Email format is not valid"),
    INVALID_PHONE_FORMAT("INVALID_PHONE_FORMAT", "Phone number format is not valid"),
    INVALID_COUNTRY_CODE("INVALID_COUNTRY_CODE", "Country code is not valid"),
    INVALID_ISO_CODE("INVALID_ISO_CODE", "ISO code is not valid"),
    INVALID_TIMEZONE("INVALID_TIMEZONE", "Timezone is not a valid IANA zone id"),
    INVALID_VIN_FORMAT("INVALID_VIN_FORMAT", "VIN format is not valid"),
    INVALID_PLATES_FORMAT("INVALID_PLATES_FORMAT", "License plates format is not valid"),
    
    DATE_IN_FUTURE("DATE_IN_FUTURE", "Date cannot be in the future"),
    DATE_IN_PAST("DATE_IN_PAST", "Date cannot be in the past"),
    DATE_TOO_FAR_IN_FUTURE("DATE_TOO_FAR_IN_FUTURE", "Date cannot be in the fat future"),
    DATE_TOO_OLD("DATE_TOO_OLD", "Date is too old"),
    EXPIRATION_DATE_TOO_OLD("EXPIRATION_DATE_TOO_OLD", "Expiration date cannot be more than 20 years old"),

    INVALID_TIME_RANGE("INVALID_TIME_RANGE", "Time range lower bound must be before upper bound"),


    INVALID_FLAT_NUMBER("INVALID_FLAT_NUMBER", "Flat number cannot be empty or blank"),

    INVALID_PASSWORD_FORMAT("INVALID_PASSWORD_FORMAT", "Password format is not valid"),
    INVALID_CODE_FORMAT("INVALID_CODE_FORMAT", "Verification code must be 6 digits"),
    VALUE_OUT_OF_RANGE("VALUE_OUT_OF_RANGE", "Value is out of allowed range"),
    VALIDATION_FAILED("VALIDATION_FAILED", "Validation failed");

    private final String code;
    private final String defaultMessage;

    @Override
    public String code() {
        return code;
    }

    @Override
    public ErrorCategory category() {
        return ErrorCategory.VALIDATION;
    }
}

