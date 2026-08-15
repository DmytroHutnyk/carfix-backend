package com.hutnyk.carfix.booking.exception;

import com.hutnyk.carfix.exception.ErrorCategory;
import com.hutnyk.carfix.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BookingErrorCode implements ErrorCode {

    BOOKING_NOT_FOUND(ErrorCategory.NOT_FOUND),
    BOOKING_CANCELLATION_NOT_ALLOWED(ErrorCategory.CONFLICT),
    INVALID_BOOKING_REQUEST(ErrorCategory.VALIDATION),
    SLOT_NOT_AVAILABLE(ErrorCategory.CONFLICT);

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
