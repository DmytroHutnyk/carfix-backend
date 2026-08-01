package com.hutnyk.carfix.exception;

import lombok.Getter;

import java.util.Map;

/**
 * The request collides with state that already exists e.g a taken email, an overlapping booking,
 * a bay already reserved for that slot. Maps to 409.
 */
@Getter
public abstract class ConflictException extends CarFixException {

    //Nullable
    private final String fieldName;
    //Nullable
    private final Object rejectedValue;

    /**
     * @param fieldName the request field the client should highlight, as the client spelled it
     * @param rejectedValue the colliding value, for logging — never published to the client
     */
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
