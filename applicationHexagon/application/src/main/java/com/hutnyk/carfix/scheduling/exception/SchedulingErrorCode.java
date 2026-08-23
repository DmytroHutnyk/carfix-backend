package com.hutnyk.carfix.scheduling.exception;

import com.hutnyk.carfix.exception.ErrorCategory;
import com.hutnyk.carfix.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SchedulingErrorCode implements ErrorCode {

    INVALID_SLOT_QUERY(ErrorCategory.VALIDATION),
    SERVICE_NOT_FOUND(ErrorCategory.NOT_FOUND);

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
