package com.hutnyk.carfix.review;

import com.hutnyk.carfix.booking.Booking;
import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.BookingStatus;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.in.review.ReviewPortIn;
import com.hutnyk.carfix.in.review.commands.AddReviewCommand;
import com.hutnyk.carfix.out.booking.BookingPortOut;
import com.hutnyk.carfix.out.branch.BranchPortOut;
import com.hutnyk.carfix.out.customer.CustomerPortOut;
import com.hutnyk.carfix.out.review.ReviewPortOut;
import com.hutnyk.carfix.review.exception.ReviewAlreadyExistsException;
import com.hutnyk.carfix.review.exception.ReviewNotAllowedException;
import com.hutnyk.carfix.review.exception.ReviewedBookingNotFoundException;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@RequiredArgsConstructor
@ApplicationService
public class ReviewService implements ReviewPortIn {

    private final ReviewPortOut reviewPortOut;
    private final BranchPortOut branchPortOut;
    private final BookingPortOut bookingPortOut;
    private final CustomerPortOut customerPortOut;
    private final Clock clock;

    @Override
    public Review addReview(String customerEmail, AddReviewCommand command) {
        BookingId bookingId = command.bookingId();
        UUID customerId = customerPortOut.loadCustomerByUsername(customerEmail).getUser().getId().id();

        Booking booking = bookingPortOut.findByIdAndCustomerId(bookingId.id(), customerId)
                .orElseThrow(() -> new ReviewedBookingNotFoundException(bookingId));

        BookingStatus effective = booking.effectiveStatus(branchNow(booking.getBranchId()));
        if (effective != BookingStatus.COMPLETED) {
            throw new ReviewNotAllowedException(bookingId, effective);
        }

        if (reviewPortOut.existsByBookingId(bookingId)) {
            throw new ReviewAlreadyExistsException(bookingId);
        }

        Review review = Review.of(ReviewId.genId(), command.starsNumber(), command.contents(), bookingId);
        Review inserted = reviewPortOut.insert(review);

        refreshRating(booking.getBranchId());

        return inserted;
    }

    private LocalDateTime branchNow(BranchId branchId) {
        ZoneId zone = branchPortOut.findActiveBranchZone(branchId).orElse(clock.getZone());
        return LocalDateTime.now(clock.withZone(zone));
    }

    private void refreshRating(BranchId branchId) {
        branchPortOut.updateRating(branchId, BranchRating.of(reviewPortOut.findStarsByBranchId(branchId)));
    }
}
