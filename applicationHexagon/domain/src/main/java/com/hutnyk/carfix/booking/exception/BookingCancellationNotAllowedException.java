package com.hutnyk.carfix.booking.exception;

import com.hutnyk.carfix.booking.BookingStatus;
import com.hutnyk.carfix.exception.ConflictException;

import java.util.UUID;

public class BookingCancellationNotAllowedException extends ConflictException {

    public BookingCancellationNotAllowedException(UUID bookingId, BookingStatus status) {
        super(BookingErrorCode.BOOKING_CANCELLATION_NOT_ALLOWED,
                "Booking " + bookingId + " cannot be cancelled from status " + status,
                null, null);
    }
}
