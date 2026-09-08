package com.hutnyk.carfix.review.mapper;

import com.hutnyk.carfix.review.Review;
import com.hutnyk.carfix.review.dto.response.ReviewResponse;

public final class ReviewResponseMapper {

    private ReviewResponseMapper() {
    }

    public static ReviewResponse toResponse(Review review) {
        if (review == null) {
            return null;
        }
        return new ReviewResponse(
                review.getId().id(),
                review.getStarsNumber(),
                review.getContents(),
                review.getBookingId().id());
    }
}
