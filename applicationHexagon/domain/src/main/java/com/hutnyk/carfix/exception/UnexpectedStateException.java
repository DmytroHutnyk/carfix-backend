package com.hutnyk.carfix.exception;

/** Internal invariant failed for reasons outside caller control. Maps to 500. */
public class UnexpectedStateException extends CarFixException {

    public UnexpectedStateException(String message) {
        super(CoreErrorCode.INTERNAL_ERROR, message);
    }

    public UnexpectedStateException(String message, Throwable cause) {
        super(CoreErrorCode.INTERNAL_ERROR, message, cause);
    }
}
