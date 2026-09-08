package com.hutnyk.carfix.review.mapper;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.in.review.commands.AddReviewCommand;
import com.hutnyk.carfix.review.dto.request.AddReviewRequest;

import java.util.UUID;

public final class AddReviewCommandMapper {

    private AddReviewCommandMapper() {
    }

    public static AddReviewCommand toCommand(UUID bookingId, AddReviewRequest request) {
        if (request == null) {
            return null;
        }
        return new AddReviewCommand(BookingId.of(bookingId), request.rating(), request.comment());
    }
}
