package com.hutnyk.carfix.exception;

import lombok.Getter;

import java.util.Map;

/**
 * A value did not satisfy a format rule or an invariant.
 * <p>
 * Abstract on purpose: every concrete validation failure names itself and supplies its own
 * {@link ErrorCode}.
 */
@Getter
public abstract class ValidationException extends CarFixException {

    //Nullable
    private final String fieldName;
    //Nullable
    private final Object rejectedValue;

    protected ValidationException(ErrorCode errorCode, String message, String fieldName, Object rejectedValue) {
        super(errorCode, message);
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Publishes the failing field and its reason. {@code rejectedValue} is deliberately not
     * published on its own as it may already appear inside the message.
     */
    @Override
    public Map<String, String> details() {
        return fieldName == null ? Map.of() : Map.of(fieldName, getMessage());
    }
}
