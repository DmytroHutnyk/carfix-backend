package com.hutnyk.carfix.booking.exception;

import com.hutnyk.carfix.booking.BookingStatus;
import com.hutnyk.carfix.exception.ConflictException;

import java.util.UUID;

public class BookingNoShowNotAllowedException extends ConflictException {

    public BookingNoShowNotAllowedException(UUID bookingId, BookingStatus status) {
        super(BookingErrorCode.BOOKING_NO_SHOW_NOT_ALLOWED,
                "Booking " + bookingId + " cannot be marked as a no-show from status " + status,
                null, null);
    }
}
