package com.hutnyk.carfix.service.exception;

import com.hutnyk.carfix.exception.NotFoundException;

public class ServiceCategoryNotFoundException extends NotFoundException {

    public ServiceCategoryNotFoundException(Integer serviceCategoryId) {
        super(ServiceErrorCode.SERVICE_CATEGORY_NOT_FOUND, "Service category", serviceCategoryId);
    }
}
