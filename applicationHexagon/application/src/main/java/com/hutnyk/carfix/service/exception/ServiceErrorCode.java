package com.hutnyk.carfix.service.exception;

import com.hutnyk.carfix.exception.ErrorCategory;
import com.hutnyk.carfix.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ServiceErrorCode implements ErrorCode {

    SERVICE_CATEGORY_NOT_FOUND(ErrorCategory.NOT_FOUND),
    SERVICE_NOT_FOUND(ErrorCategory.NOT_FOUND),
    SERVICE_IN_USE(ErrorCategory.CONFLICT);

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
