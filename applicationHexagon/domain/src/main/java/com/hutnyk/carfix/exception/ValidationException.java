package com.hutnyk.carfix.exception;

import lombok.Getter;

import java.util.Map;

/** Base for named validation failures carrying their own ErrorCode. */
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

    // Publish field and reason only; rejected values may contain sensitive data.
    @Override
    public Map<String, String> details() {
        return fieldName == null ? Map.of() : Map.of(fieldName, getMessage());
    }
}
