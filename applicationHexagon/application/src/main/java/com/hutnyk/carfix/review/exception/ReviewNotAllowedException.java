package com.hutnyk.carfix.review.exception;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.BookingStatus;
import com.hutnyk.carfix.exception.BusinessRuleViolationException;

public class ReviewNotAllowedException extends BusinessRuleViolationException {

    public ReviewNotAllowedException(BookingId bookingId, BookingStatus status) {
        super(ReviewErrorCode.REVIEW_NOT_ALLOWED,
                "Booking " + bookingId.id() + " cannot be reviewed from status " + status);
    }
}
