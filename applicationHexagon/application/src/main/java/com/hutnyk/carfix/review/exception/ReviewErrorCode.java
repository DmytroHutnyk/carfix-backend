package com.hutnyk.carfix.review.exception;

import com.hutnyk.carfix.exception.ErrorCategory;
import com.hutnyk.carfix.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    REVIEW_ALREADY_EXISTS(ErrorCategory.CONFLICT),
    REVIEWED_BOOKING_NOT_FOUND(ErrorCategory.NOT_FOUND);

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
