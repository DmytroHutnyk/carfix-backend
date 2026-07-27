package com.hutnyk.carfix.customer.exception;

public class PhoneNumberAlreadyTakenException extends UserAlreadyExistsException {

    public PhoneNumberAlreadyTakenException(String message, Object rejectedValue) {
        super(message, rejectedValue);
    }
}
