package com.hutnyk.carfix.exceptions;

public class PhoneNumberAlreadyTakenException extends UserAlreadyExistsException {

    public PhoneNumberAlreadyTakenException(String message, Object rejectedValue) {
        super(message, rejectedValue);
    }
}
