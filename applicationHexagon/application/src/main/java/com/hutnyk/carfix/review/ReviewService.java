package com.hutnyk.carfix.review;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.in.review.ReviewPortIn;
import com.hutnyk.carfix.in.review.commands.AddReviewCommand;
import com.hutnyk.carfix.out.branch.BranchPortOut;
import com.hutnyk.carfix.out.review.ReviewPortOut;
import com.hutnyk.carfix.review.exception.ReviewAlreadyExistsException;
import com.hutnyk.carfix.review.exception.ReviewedBookingNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationService
public class ReviewService implements ReviewPortIn {

    private final ReviewPortOut reviewPortOut;
    private final BranchPortOut branchPortOut;

    /**
     * @ApplicationService already carries class-level @Transactional, so the insert and the
     * cache refresh commit together — the cache can never be left describing a review set
     * that was rolled back.
     */
    @Override
    public Review addReview(AddReviewCommand command) {
        BookingId bookingId = command.bookingId();

        if (reviewPortOut.existsByBookingId(bookingId)) {
            throw new ReviewAlreadyExistsException(bookingId);
        }
        BranchId branchId = reviewPortOut.findBranchIdByBookingId(bookingId)
                .orElseThrow(() -> new ReviewedBookingNotFoundException(bookingId));

        Review review = Review.of(ReviewId.genId(), command.starsNumber(), command.contents(), bookingId);
        Review inserted = reviewPortOut.insert(review);

        refreshRating(branchId);

        return inserted;
    }

    /* Read-then-recompute-then-write, after the insert so the new review is in the average.
       The rule itself is BranchRating's; this only sequences the ports around it. */
    private void refreshRating(BranchId branchId) {
        branchPortOut.updateRating(branchId, BranchRating.of(reviewPortOut.findStarsByBranchId(branchId)));
    }
}
