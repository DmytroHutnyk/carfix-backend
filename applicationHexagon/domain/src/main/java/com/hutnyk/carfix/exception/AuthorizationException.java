package com.hutnyk.carfix.exception;

/**
 * The caller is authenticated but not permitted to perform this action. Maps to 403.
 * <p>
 * Only for cases where admitting the resource exists is safe e.g. a workshop employee acting outside
 * their branch, say. When the caller should not even learn the resource exists, throw
 * {@link NotFoundException} instead.
 */
public abstract class AuthorizationException extends CarFixException {

    protected AuthorizationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
