package com.hutnyk.carfix.booking.exception;

import com.hutnyk.carfix.exception.NotFoundException;

import java.util.UUID;

public class BookingNotFoundException extends NotFoundException {

    public BookingNotFoundException(UUID bookingId) {
        super(BookingErrorCode.BOOKING_NOT_FOUND, "Booking", bookingId);
    }
}
