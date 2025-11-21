package com.hutnyk.carfix.exception;

import lombok.Getter;
/**
 *  {@code DomainObjectValidationException} used for validation exceptions.
 */
@Getter
public class DomainObjectValidationException extends InvalidDomainObjectException {
    private final ValidationErrorType errorType;
    private final String fieldName;
    private final Object rejectedValue;

    /**
     * Validation exception constructor with errorType, fieldName and rejectedValue.
     * @param errorType
     * @param fieldName
     * @param rejectedValue
     */
    public DomainObjectValidationException(ValidationErrorType errorType, String fieldName, Object rejectedValue) {
        super(buildMessage(errorType, fieldName, rejectedValue));
        this.errorType = errorType;
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }

    /**
     * Validation exception constructor with errorType, fieldName, without rejectedValue. RejectedValue = null.
     * @param errorType
     * @param fieldName
     */
    public DomainObjectValidationException(ValidationErrorType errorType, String fieldName) {
        super(buildMessage(errorType, fieldName, null));
        this.errorType = errorType;
        this.fieldName = fieldName;
        this.rejectedValue = null;
    }

    /**
     * Validation exception constructor with errorType, fieldName, rejectedValue and custom message provided.
     * @param errorType
     * @param fieldName
     * @param customMessage
     */
    public DomainObjectValidationException(ValidationErrorType errorType,
                                           String fieldName,
                                           Object rejectedValue,
                                           String customMessage) {
        super(customMessage);
        this.errorType = errorType;
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }


    /**
     * Builds a message from parameters passed as arguments, matching class fields.
     * @param errorType
     * @param fieldName
     * @param rejectedValue
     * @return {@code String}
     */
    private static String buildMessage(ValidationErrorType errorType, String fieldName, Object rejectedValue){
        StringBuilder message = new StringBuilder();

        if(fieldName != null){
            message.append(fieldName).append(": ");
        }

        message.append(errorType.getDefaultMessage());

        if(rejectedValue != null){
            message.append(" (received: ").append(rejectedValue).append(")");
        }

        return message.toString();
    }
}
