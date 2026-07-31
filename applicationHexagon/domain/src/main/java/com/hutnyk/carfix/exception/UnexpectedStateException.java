package com.hutnyk.carfix.exception;

/**
 * An invariant of our own code broke: a row that was just written cannot be read back, an enum
 * constant has no branch, a principal survived the record it points at. Maps to 500.
 * <p>
 * We never use it for anything the caller could have caused: that is a validation, conflict, or
 * business-rule failure.
 */
public class UnexpectedStateException extends CarFixException {

    public UnexpectedStateException(String message) {
        super(CoreErrorCode.INTERNAL_ERROR, message);
    }

    public UnexpectedStateException(String message, Throwable cause) {
        super(CoreErrorCode.INTERNAL_ERROR, message, cause);
    }
}
