package com.hutnyk.carfix.user.exception;

import com.hutnyk.carfix.exception.AuthenticationFailedException;

/** Missing principal backing data maps to 401 so client drops stale session. */
public class AuthenticatedUserMissingException extends AuthenticationFailedException {

    private AuthenticatedUserMissingException(String message) {
        super(UserErrorCode.AUTHENTICATED_USER_MISSING, message);
    }

    public static AuthenticatedUserMissingException forEmail(String email) {
        return new AuthenticatedUserMissingException("Authenticated user not found: " + email);
    }

    public static AuthenticatedUserMissingException noCustomerAggregate(String username) {
        return new AuthenticatedUserMissingException(
                "No customer aggregate for authenticated principal: " + username);
    }

    public static AuthenticatedUserMissingException noOwnerAggregate(String username) {
        return new AuthenticatedUserMissingException(
                "No owner aggregate for authenticated principal: " + username);
    }
}
