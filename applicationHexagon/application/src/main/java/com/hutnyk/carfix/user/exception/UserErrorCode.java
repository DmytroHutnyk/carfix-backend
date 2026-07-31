package com.hutnyk.carfix.user.exception;

import com.hutnyk.carfix.exception.ErrorCategory;
import com.hutnyk.carfix.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    EMAIL_ALREADY_TAKEN(ErrorCategory.CONFLICT),
    PHONE_NUMBER_ALREADY_TAKEN(ErrorCategory.CONFLICT),
    AUTHENTICATED_USER_MISSING(ErrorCategory.AUTHENTICATION);

    private final ErrorCategory category;

    @Override
    public String code() {
        return name();
    }

    @Override
    public ErrorCategory category() {
        return category;
    }
}
