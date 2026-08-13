package com.hutnyk.carfix.scheduling.exception;

import com.hutnyk.carfix.exception.NotFoundException;

import java.util.List;

/**
 * At least one requested service is missing, not ACTIVE, or belongs to another branch.
 * The whole requested set is echoed — naming the offending id would confirm which ids are real.
 */
public class ServiceNotFoundException extends NotFoundException {

    public ServiceNotFoundException(List<Integer> serviceIds) {
        super(SchedulingErrorCode.SERVICE_NOT_FOUND, "Service", serviceIds);
    }
}
