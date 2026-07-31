package com.hutnyk.carfix.user.exception;

public class EmailAlreadyTakenException extends UserAlreadyExistsException {

    public EmailAlreadyTakenException(String email) {
        super(UserErrorCode.EMAIL_ALREADY_TAKEN,
                "User with " + email + " email already exists",
                "email",
                email);
    }
}
