package com.hutnyk.carfix.exception;

/**
 * Identity of a failure.
 * <p>
 * Implemented by one enum per feature (precedent: {@link CoreErrorCode}, {@code CarProfileErrorCode}),
 * so a new feature ships its own enum instead of editing a file every feature shares.
 * <p>
 * {@link #code()} is published to clients in the {@code code} property of the error response.
 */
public interface ErrorCode {

    String code();

    /** The kind of failure, which is what the web layer turns into an HTTP status. */
    ErrorCategory category();
}
