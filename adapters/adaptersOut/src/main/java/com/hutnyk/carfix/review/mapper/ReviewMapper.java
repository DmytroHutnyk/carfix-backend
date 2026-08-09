package com.hutnyk.carfix.review.mapper;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.entity.BookingEntity;
import com.hutnyk.carfix.review.Review;
import com.hutnyk.carfix.review.ReviewId;
import com.hutnyk.carfix.review.entity.ReviewEntity;

public final class ReviewMapper {

    private ReviewMapper() {
    }

    public static Review toDomain(ReviewEntity entity) {
        if (entity == null) {
            return null;
        }
        return Review.of(
                ReviewId.of(entity.getId()),
                entity.getStarsNumber(),
                entity.getContents(),
                BookingId.of(entity.getBookingEntity().getId()));
    }

    public static ReviewEntity toEntity(Review review, BookingEntity bookingEntity) {
        if (review == null) {
            return null;
        }
        ReviewEntity entity = new ReviewEntity();
        entity.setId(review.getId().id());
        entity.setStarsNumber(review.getStarsNumber());
        entity.setContents(review.getContents());
        entity.setBookingEntity(bookingEntity);
        return entity;
    }
}
