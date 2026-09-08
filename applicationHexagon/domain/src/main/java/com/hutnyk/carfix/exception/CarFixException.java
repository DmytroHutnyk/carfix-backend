package com.hutnyk.carfix.exception;

import lombok.Getter;

import java.util.Map;

/** Root for deliberate failures; feature exceptions extend a category subclass, never this directly. */
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

    // Feature exceptions override this only when a client field can be identified.
    public Map<String, String> details() {
        return Map.of();
    }

    public ErrorCategory category() {
        return errorCode.category();
    }

    // Validator cannot be used here: it throws another CarFixException.
    private static ErrorCode requireErrorCode(ErrorCode errorCode) {
        if (errorCode == null) {
            throw new IllegalArgumentException("errorCode must not be null");
        }
        return errorCode;
    }
}
