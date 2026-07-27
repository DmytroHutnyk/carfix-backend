package com.hutnyk.carfix.customer.exception;

public class EmailAlreadyTakenException extends UserAlreadyExistsException {

    public EmailAlreadyTakenException(String message, Object rejectedValue) {
        super(message, rejectedValue);
    }
}
