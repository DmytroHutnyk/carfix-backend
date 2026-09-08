package com.hutnyk.carfix.user.exception;

import com.hutnyk.carfix.exception.AuthorizationException;
import com.hutnyk.carfix.user.UserRole;

public class AccountDeletionNotAllowedException extends AuthorizationException {

    public AccountDeletionNotAllowedException(UserRole role) {
        super(UserErrorCode.ACCOUNT_DELETION_NOT_ALLOWED,
                "Account deletion is only available to customers, not " + role);
    }
}
