package com.hutnyk.carfix.serviceBay.exception;

import com.hutnyk.carfix.exception.NotFoundException;

public class ServiceBayNotFoundException extends NotFoundException {

    public ServiceBayNotFoundException(Integer serviceBayId) {
        super(ServiceBayErrorCode.SERVICE_BAY_NOT_FOUND, "Service bay", serviceBayId);
    }
}
