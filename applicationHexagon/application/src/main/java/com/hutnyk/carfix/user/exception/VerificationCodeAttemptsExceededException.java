package com.hutnyk.carfix.user.exception;

import com.hutnyk.carfix.exception.BusinessRuleViolationException;

public class VerificationCodeAttemptsExceededException extends BusinessRuleViolationException {

    public VerificationCodeAttemptsExceededException() {
        super(UserErrorCode.VERIFICATION_CODE_ATTEMPTS_EXCEEDED, "Too many incorrect attempts. Request a new code");
    }
}
