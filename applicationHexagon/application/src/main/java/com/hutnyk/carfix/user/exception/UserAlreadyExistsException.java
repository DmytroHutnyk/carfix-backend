package com.hutnyk.carfix.user.exception;

import com.hutnyk.carfix.exception.ConflictException;
import com.hutnyk.carfix.exception.ErrorCode;

/**
 * A user account already occupies one of the identifiers the registration tried to claim.
 */
public abstract class UserAlreadyExistsException extends ConflictException {

    protected UserAlreadyExistsException(ErrorCode errorCode, String message, String fieldName, Object rejectedValue) {
        super(errorCode, message, fieldName, rejectedValue);
    }
}
