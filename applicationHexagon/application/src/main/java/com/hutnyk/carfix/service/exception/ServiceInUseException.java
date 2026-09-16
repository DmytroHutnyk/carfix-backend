package com.hutnyk.carfix.service.exception;

import com.hutnyk.carfix.exception.ConflictException;

public class ServiceInUseException extends ConflictException {

    public ServiceInUseException(Integer serviceId) {
        super(ServiceErrorCode.SERVICE_IN_USE, "Service is referenced by a booking", "service", serviceId);
    }
}
