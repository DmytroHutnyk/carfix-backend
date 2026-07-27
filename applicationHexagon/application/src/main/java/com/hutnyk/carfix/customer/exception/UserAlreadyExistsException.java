package com.hutnyk.carfix.customer.exception;

import lombok.Getter;

@Getter
public class UserAlreadyExistsException extends RuntimeException {

    private final Object rejectedValue;

    public UserAlreadyExistsException(String message, Object rejectedValue) {
        super(message);
        this.rejectedValue = rejectedValue;
    }
}
