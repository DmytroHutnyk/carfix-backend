package com.hutnyk.carfix.exception;

import lombok.Getter;

/**
 * The domain rejected a value.
 *
 * Carries its {@link ValidationErrorType} both as the human-readable default message and as the
 * published {@link ErrorCode}.
 */
@Getter
public class DomainObjectValidationException extends ValidationException {

    private final ValidationErrorType errorType;

    /**
     * Validation exception with errorType, fieldName and rejectedValue.
     *
     * @param errorType the rule that was broken
     * @param fieldName the field that broke it
     * @param rejectedValue the offending value — never pass a secret, it lands in the message
     */
    public DomainObjectValidationException(ValidationErrorType errorType, String fieldName, Object rejectedValue) {
        super(errorType, buildMessage(errorType, fieldName, rejectedValue), fieldName, rejectedValue);
        this.errorType = errorType;
    }

    /**
     * Validation exception with errorType and fieldName, without rejectedValue.
     *
     * @param errorType the rule that was broken
     * @param fieldName the field that broke it
     */
    public DomainObjectValidationException(ValidationErrorType errorType, String fieldName) {
        super(errorType, buildMessage(errorType, fieldName, null), fieldName, null);
        this.errorType = errorType;
    }

    /**
     * Validation exception with errorType, fieldName, rejectedValue and a custom message.
     *
     * @param errorType the rule that was broken
     * @param fieldName the field that broke it
     * @param rejectedValue the offending value
     * @param customMessage replaces the message built from the error type
     */
    public DomainObjectValidationException(ValidationErrorType errorType,
                                           String fieldName,
                                           Object rejectedValue,
                                           String customMessage) {
        super(errorType, customMessage, fieldName, rejectedValue);
        this.errorType = errorType;
    }

    /**
     * Builds a message from parameters passed as arguments, matching class fields.
     *
     * @return {@code String}
     */
    private static String buildMessage(ValidationErrorType errorType, String fieldName, Object rejectedValue) {
        StringBuilder message = new StringBuilder();

        if (fieldName != null) {
            message.append(fieldName).append(": ");
        }

        message.append(errorType.getDefaultMessage());

        if (rejectedValue != null) {
            message.append(" (received: ").append(rejectedValue).append(")");
        }

        return message.toString();
    }
}
