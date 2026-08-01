package com.hutnyk.carfix.exception;

/**
 * The caller's identity could not be established, or a previously established one no longer holds
 * (session points at a deleted user, credentials rejected, verification pending). Maps to 401.
 * <p>
 * Named {@code AuthenticationFailedException} rather than {@code AuthenticationException} to stay
 * Messages here reach the client: never reveal <em>which</em> factor failed, and never reveal
 * whether an account exists.
 */
public abstract class AuthenticationFailedException extends CarFixException {

    protected AuthenticationFailedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
