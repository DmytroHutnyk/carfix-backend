package com.hutnyk.carfix.review.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.entity.BookingEntity;
import com.hutnyk.carfix.in.branch.query.BranchReviewView;
import com.hutnyk.carfix.review.Review;
import com.hutnyk.carfix.review.ReviewId;
import com.hutnyk.carfix.review.entity.ReviewEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

public class ReviewMapperTest {

    private static final UUID REVIEW_ID = UUID.randomUUID();
    private static final UUID BOOKING_ID = UUID.randomUUID();

    private static BookingEntity bookingEntity() {
        BookingEntity booking = new BookingEntity();
        booking.setId(BOOKING_ID);
        return booking;
    }

    @Test
    void toDomain_maps_every_field() {
        ReviewEntity entity = new ReviewEntity();
        entity.setId(REVIEW_ID);
        entity.setStarsNumber(4);
        entity.setContents("Fast and honest");
        entity.setBookingEntity(bookingEntity());

        Review review = ReviewMapper.toDomain(entity);

        assertThat(review.getId()).isEqualTo(ReviewId.of(REVIEW_ID));
        assertThat(review.getStarsNumber()).isEqualTo(4);
        assertThat(review.getContents()).isEqualTo("Fast and honest");
        assertThat(review.getBookingId()).isEqualTo(BookingId.of(BOOKING_ID));
    }

    @Test
    void toDomain_keeps_null_contents_null() {
        ReviewEntity entity = new ReviewEntity();
        entity.setId(REVIEW_ID);
        entity.setStarsNumber(5);
        entity.setContents(null);
        entity.setBookingEntity(bookingEntity());

        Review review = ReviewMapper.toDomain(entity);

        assertThat(review.getContents()).isNull();
    }

    @Test
    void toEntity_maps_every_field_and_attaches_the_booking_reference() {
        Review review = Review.of(ReviewId.of(REVIEW_ID), 3, "Average", BookingId.of(BOOKING_ID));
        BookingEntity booking = bookingEntity();

        ReviewEntity entity = ReviewMapper.toEntity(review, booking);

        assertThat(entity.getId()).isEqualTo(REVIEW_ID);
        assertThat(entity.getStarsNumber()).isEqualTo(3);
        assertThat(entity.getContents()).isEqualTo("Average");
        assertThat(entity.getBookingEntity()).isSameAs(booking);
    }

    @Test
    void null_input_maps_to_null() {
        assertThat(ReviewMapper.toDomain(null)).isNull();
        assertThat(ReviewMapper.toEntity(null, bookingEntity())).isNull();
    }

    @Test
    void toBranchReviewView_copies_fields() {
        ReviewEntity entity = new ReviewEntity();
        entity.setId(REVIEW_ID);
        entity.setStarsNumber(5);
        entity.setContents("Excellent service!");
        entity.setCreatedAt(Instant.parse("2026-01-15T18:30:00Z"));

        BranchReviewView view = ReviewMapper.toBranchReviewView(entity, "Jan", "Kowalski");

        assertThat(view.reviewId()).isEqualTo(REVIEW_ID);
        assertThat(view.starsNumber()).isEqualTo(5);
        assertThat(view.contents()).isEqualTo("Excellent service!");
        assertThat(view.createdAt()).isEqualTo(Instant.parse("2026-01-15T18:30:00Z"));
        assertThat(view.customerName()).isEqualTo("Jan");
        assertThat(view.customerSurname()).isEqualTo("Kowalski");
    }
}
