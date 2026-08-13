package com.hutnyk.carfix.in.review.commands;

import com.hutnyk.carfix.booking.BookingId;

public record AddReviewCommand(
        BookingId bookingId,
        Integer starsNumber,
        //Nullable
        String contents
) {}
