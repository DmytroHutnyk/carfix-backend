package com.hutnyk.carfix.user.exception;

import com.hutnyk.carfix.exception.ConflictException;

public class EmailAlreadyVerifiedException extends ConflictException {

    public EmailAlreadyVerifiedException(String email) {
        super(UserErrorCode.EMAIL_ALREADY_VERIFIED,
                "Email " + email + " is already verified",
                null, null);
    }
}
