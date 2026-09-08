package com.hutnyk.carfix.exception;

import lombok.Getter;

/** Outbound dependency failed, timed out, or returned an invalid response. Maps to 502. */
@Getter
public abstract class ExternalServiceException extends CarFixException {

    private final String serviceName;

    protected ExternalServiceException(ErrorCode errorCode, String serviceName, String message, Throwable cause) {
        super(errorCode, message, cause);
        this.serviceName = serviceName;
    }
}
