package com.hutnyk.carfix.exception;

/** Authenticated caller lacks permission. Use NotFoundException when existence must stay hidden. */
public abstract class AuthorizationException extends CarFixException {

    protected AuthorizationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
