package com.hutnyk.carfix.scheduling.exception;

import com.hutnyk.carfix.exception.NotFoundException;

import java.util.List;

/** Echoes requested set without revealing which service is missing, inactive, or foreign. */
public class ServiceNotFoundException extends NotFoundException {

    public ServiceNotFoundException(List<Integer> serviceIds) {
        super(SchedulingErrorCode.SERVICE_NOT_FOUND, "Service", serviceIds);
    }
}
