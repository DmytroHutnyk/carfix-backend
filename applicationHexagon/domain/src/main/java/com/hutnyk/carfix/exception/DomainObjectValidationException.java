package com.hutnyk.carfix.exception;

import lombok.Getter;

/** Domain validation failure whose rule is also its published error code. */
@Getter
public class DomainObjectValidationException extends ValidationException {

    private final ValidationErrorType errorType;

    // Never pass a secret as rejectedValue; it is included in the message.
    public DomainObjectValidationException(ValidationErrorType errorType, String fieldName, Object rejectedValue) {
        super(errorType, buildMessage(errorType, fieldName, rejectedValue), fieldName, rejectedValue);
        this.errorType = errorType;
    }

    public DomainObjectValidationException(ValidationErrorType errorType, String fieldName) {
        super(errorType, buildMessage(errorType, fieldName, null), fieldName, null);
        this.errorType = errorType;
    }

    public DomainObjectValidationException(ValidationErrorType errorType,
                                           String fieldName,
                                           Object rejectedValue,
                                           String customMessage) {
        super(errorType, customMessage, fieldName, rejectedValue);
        this.errorType = errorType;
    }

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
