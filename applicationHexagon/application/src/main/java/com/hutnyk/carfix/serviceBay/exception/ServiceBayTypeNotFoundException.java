package com.hutnyk.carfix.serviceBay.exception;

import com.hutnyk.carfix.exception.NotFoundException;

public class ServiceBayTypeNotFoundException extends NotFoundException {

    public ServiceBayTypeNotFoundException(Integer serviceBayTypeId) {
        super(ServiceBayErrorCode.SERVICE_BAY_TYPE_NOT_FOUND, "Service bay type", serviceBayTypeId);
    }

    public ServiceBayTypeNotFoundException(String name) {
        super(ServiceBayErrorCode.SERVICE_BAY_TYPE_NOT_FOUND, "Service bay type", name);
    }
}
