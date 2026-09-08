package com.hutnyk.carfix.user.exception;

import com.hutnyk.carfix.exception.ErrorCategory;
import com.hutnyk.carfix.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    EMAIL_ALREADY_TAKEN(ErrorCategory.CONFLICT),
    PHONE_NUMBER_ALREADY_TAKEN(ErrorCategory.CONFLICT),
    AUTHENTICATED_USER_MISSING(ErrorCategory.AUTHENTICATION),
    EMAIL_ALREADY_VERIFIED(ErrorCategory.CONFLICT),
    VERIFICATION_CODE_NOT_FOUND(ErrorCategory.NOT_FOUND),
    VERIFICATION_CODE_RESEND_TOO_SOON(ErrorCategory.BUSINESS_RULE),
    VERIFICATION_CODE_EXPIRED(ErrorCategory.BUSINESS_RULE),
    VERIFICATION_CODE_ATTEMPTS_EXCEEDED(ErrorCategory.BUSINESS_RULE),
    VERIFICATION_CODE_INVALID(ErrorCategory.VALIDATION),
    ACCOUNT_DELETION_NOT_ALLOWED(ErrorCategory.AUTHORIZATION);

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
