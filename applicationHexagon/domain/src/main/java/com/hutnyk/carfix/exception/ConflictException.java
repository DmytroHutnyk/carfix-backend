package com.hutnyk.carfix.exception;

import lombok.Getter;

import java.util.Map;

/** Request conflicts with existing state. Maps to 409. */
@Getter
public abstract class ConflictException extends CarFixException {

    //Nullable
    private final String fieldName;
    //Nullable
    private final Object rejectedValue;

    // rejectedValue is log-only; never publish it to clients.
    protected ConflictException(ErrorCode errorCode, String message, String fieldName, Object rejectedValue) {
        super(errorCode, message);
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }

    @Override
    public Map<String, String> details() {
        return fieldName == null ? Map.of() : Map.of(fieldName, getMessage());
    }
}
