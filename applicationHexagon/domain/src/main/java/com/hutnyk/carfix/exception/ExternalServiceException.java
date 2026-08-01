package com.hutnyk.carfix.exception;

import lombok.Getter;

/**
 * An outbound dependency: payment provider, mail gateway, geocoding API failed, timed out, or
 * answered nonsense. Maps to 502.
 */
@Getter
public abstract class ExternalServiceException extends CarFixException {

    private final String serviceName;

    protected ExternalServiceException(ErrorCode errorCode, String serviceName, String message, Throwable cause) {
        super(errorCode, message, cause);
        this.serviceName = serviceName;
    }
}
