package com.hutnyk.carfix.service.exception;

import com.hutnyk.carfix.exception.NotFoundException;

public class ServiceNotFoundException extends NotFoundException {

    public ServiceNotFoundException(Integer serviceId) {
        super(ServiceErrorCode.SERVICE_NOT_FOUND, "Service", serviceId);
    }
}
