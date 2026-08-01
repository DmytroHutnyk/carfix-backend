package com.hutnyk.carfix.exception;

import lombok.RequiredArgsConstructor;

/**
 * Codes for failures belonging to no single feature.
 */
@RequiredArgsConstructor
public enum CoreErrorCode implements ErrorCode {

    VALIDATION_FAILED(ErrorCategory.VALIDATION),
    MALFORMED_REQUEST(ErrorCategory.VALIDATION),
    NOT_FOUND(ErrorCategory.NOT_FOUND),
    CONFLICT(ErrorCategory.CONFLICT),
    AUTHENTICATION_FAILED(ErrorCategory.AUTHENTICATION),
    ACCESS_DENIED(ErrorCategory.AUTHORIZATION),
    BUSINESS_RULE_VIOLATED(ErrorCategory.BUSINESS_RULE),
    EXTERNAL_SERVICE_FAILED(ErrorCategory.INTEGRATION),
    INTERNAL_ERROR(ErrorCategory.INTERNAL);

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
