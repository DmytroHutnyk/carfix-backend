package com.hutnyk.carfix.booking.exception;

import com.hutnyk.carfix.exception.ValidationException;

/** Invalid booking input names one field for client highlighting. */
public class InvalidBookingRequestException extends ValidationException {

    public InvalidBookingRequestException(String fieldName, String reason) {
        super(BookingErrorCode.INVALID_BOOKING_REQUEST, "Invalid booking request: " + reason, fieldName, null);
    }
}
