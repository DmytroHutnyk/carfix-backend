package com.hutnyk.carfix.booking.exception;

import com.hutnyk.carfix.exception.ValidationException;

public class InvalidBranchBookingQueryException extends ValidationException {

    public InvalidBranchBookingQueryException(String reason) {
        super(BookingErrorCode.INVALID_BRANCH_BOOKING_QUERY, "Invalid booking query: " + reason, null, null);
    }
}
