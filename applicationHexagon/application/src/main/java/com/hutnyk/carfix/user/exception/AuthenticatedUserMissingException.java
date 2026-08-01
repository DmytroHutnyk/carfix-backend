package com.hutnyk.carfix.user.exception;

import com.hutnyk.carfix.exception.AuthenticationFailedException;

/**
 * A live session names a principal whose backing record is gone.
 * <p>
 * Answers 401 rather than 500 on purpose: the session is what is broken, so the client's correct
 * move is to drop it and log in again — which is exactly what the web client does on 401.
 */
public class AuthenticatedUserMissingException extends AuthenticationFailedException {

    private AuthenticatedUserMissingException(String message) {
        super(UserErrorCode.AUTHENTICATED_USER_MISSING, message);
    }

    /** No user row for the authenticated principal. */
    public static AuthenticatedUserMissingException forEmail(String email) {
        return new AuthenticatedUserMissingException("Authenticated user not found: " + email);
    }

    /** A user row exists, but the role-specific aggregate it needs does not. */
    public static AuthenticatedUserMissingException noCustomerAggregate(String username) {
        return new AuthenticatedUserMissingException(
                "No customer aggregate for authenticated principal: " + username);
    }
}
