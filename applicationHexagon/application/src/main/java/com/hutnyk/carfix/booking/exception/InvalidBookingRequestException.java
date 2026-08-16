package com.hutnyk.carfix.booking.exception;

import com.hutnyk.carfix.exception.ValidationException;

/**
 * The booking request is not answerable as asked: chain size/duplicates, missing date or start,
 * a start that is not on the 15-minute grid, or a start before the branch-local now. Names the
 * offending field so the client can highlight it.
 */
public class InvalidBookingRequestException extends ValidationException {

    public InvalidBookingRequestException(String fieldName, String reason) {
        super(BookingErrorCode.INVALID_BOOKING_REQUEST, "Invalid booking request: " + reason, fieldName, null);
    }
}
