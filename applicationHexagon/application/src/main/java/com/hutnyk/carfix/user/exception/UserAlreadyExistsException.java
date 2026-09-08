package com.hutnyk.carfix.user.exception;

import com.hutnyk.carfix.exception.ConflictException;
import com.hutnyk.carfix.exception.ErrorCode;

public abstract class UserAlreadyExistsException extends ConflictException {

    protected UserAlreadyExistsException(ErrorCode errorCode, String message, String fieldName, Object rejectedValue) {
        super(errorCode, message, fieldName, rejectedValue);
    }
}
