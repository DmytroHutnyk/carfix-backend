package com.hutnyk.carfix.exceptions;

public class EmailAlreadyTakenException extends UserAlreadyExistsException {

    public EmailAlreadyTakenException(String message, Object rejectedValue) {
        super(message, rejectedValue);
    }
}
