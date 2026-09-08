package com.hutnyk.carfix.serviceBay.exception;

import com.hutnyk.carfix.exception.ErrorCategory;
import com.hutnyk.carfix.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ServiceBayErrorCode implements ErrorCode {

    SERVICE_BAY_NOT_FOUND(ErrorCategory.NOT_FOUND),
    SERVICE_BAY_TYPE_NOT_FOUND(ErrorCategory.NOT_FOUND);

    private final ErrorCategory category;

    @Override
    public String code() {
        return name();
    }

    @Override
    public ErrorCategory category() {
        return category;
    }
}
