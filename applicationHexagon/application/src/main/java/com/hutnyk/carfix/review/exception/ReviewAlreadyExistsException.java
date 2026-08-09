package com.hutnyk.carfix.review.exception;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.exception.ConflictException;

public class ReviewAlreadyExistsException extends ConflictException {

    public ReviewAlreadyExistsException(BookingId bookingId) {
        super(ReviewErrorCode.REVIEW_ALREADY_EXISTS,
                "This booking has already been reviewed",
                "bookingId",
                bookingId.id());
    }
}
