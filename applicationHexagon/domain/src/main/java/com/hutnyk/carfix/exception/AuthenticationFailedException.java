package com.hutnyk.carfix.exception;

/** Identity could not be established or no longer holds. Client messages must not reveal which factor failed. */
public abstract class AuthenticationFailedException extends CarFixException {

    protected AuthenticationFailedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
