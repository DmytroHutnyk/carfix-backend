package com.hutnyk.carfix.user.exception;

import com.hutnyk.carfix.exception.BusinessRuleViolationException;

public class VerificationCodeExpiredException extends BusinessRuleViolationException {

    public VerificationCodeExpiredException() {
        super(UserErrorCode.VERIFICATION_CODE_EXPIRED, "The verification code has expired. Request a new one");
    }
}
