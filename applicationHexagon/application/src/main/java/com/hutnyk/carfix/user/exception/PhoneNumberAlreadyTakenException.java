package com.hutnyk.carfix.user.exception;

import com.hutnyk.carfix.user.PhoneNumber;

public class PhoneNumberAlreadyTakenException extends UserAlreadyExistsException {

    public PhoneNumberAlreadyTakenException(PhoneNumber phoneNumber) {
        super(UserErrorCode.PHONE_NUMBER_ALREADY_TAKEN,
                "User with " + phoneNumber.phoneNumber() + " phone number already exists",
                "phoneCountryCodeAndPhoneNumber", //TODO don't make it hardcoded?
                phoneNumber);
    }
}
