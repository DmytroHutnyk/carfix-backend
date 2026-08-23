package com.hutnyk.carfix.user.exception;

import com.hutnyk.carfix.exception.NotFoundException;

public class VerificationCodeNotFoundException extends NotFoundException {

    public VerificationCodeNotFoundException() {
        super(UserErrorCode.VERIFICATION_CODE_NOT_FOUND, "No active verification code. Request a new one");
    }
}
