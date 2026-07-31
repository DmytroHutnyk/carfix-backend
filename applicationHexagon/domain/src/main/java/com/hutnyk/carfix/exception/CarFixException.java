package com.hutnyk.carfix.exception;

import lombok.Getter;

import java.util.Map;

/**
 * Root of every exception CarFix throws on purpose.
 * <p>
 * This exception should not be extended directly. Extend one of the category classes ({@link ValidationException},
 * {@link NotFoundException}, {@link ConflictException}, {@link AuthenticationFailedException},
 * {@link AuthorizationException}, {@link BusinessRuleViolationException},
 * {@link ExternalServiceException}), or throw {@link UnexpectedStateException}.
 */
@Getter
public abstract class CarFixException extends RuntimeException {

    private final ErrorCode errorCode;

    protected CarFixException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = requireErrorCode(errorCode);
    }

    protected CarFixException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = requireErrorCode(errorCode);
    }

    /**
     * Field-level detail published under the response's {@code errors} property, keyed by the field
     * name the client sent. Empty by default; categories that know which field failed override it.
     * <p>
     * @return field name to human-readable reason; empty when there is nothing field-specific to say
     */
    public Map<String, String> details() {
        return Map.of();
    }

    public ErrorCategory category() {
        return errorCode.category();
    }

    /**
     * Guards the code at construction. Deliberately not {@code Validator.notNull} — that throws a
     * {@link DomainObjectValidationException}, which is itself a {@code CarFixException}.
     */
    private static ErrorCode requireErrorCode(ErrorCode errorCode) {
        if (errorCode == null) {
            throw new IllegalArgumentException("errorCode must not be null");
        }
        return errorCode;
    }
}
