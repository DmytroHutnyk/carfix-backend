package com.hutnyk.carfix.user.exception;

import com.hutnyk.carfix.exception.ValidationException;

public class VerificationCodeInvalidException extends ValidationException {

    public VerificationCodeInvalidException(int attemptsLeft) {
        super(UserErrorCode.VERIFICATION_CODE_INVALID,
                "Incorrect verification code. " + attemptsLeft + " attempt(s) left",
                "code",
                null);
    }
}
