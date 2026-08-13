package com.hutnyk.carfix.review.exception;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.exception.NotFoundException;

public class ReviewedBookingNotFoundException extends NotFoundException {

    public ReviewedBookingNotFoundException(BookingId bookingId) {
        super(ReviewErrorCode.REVIEWED_BOOKING_NOT_FOUND, "Booking", bookingId.id());
    }
}
