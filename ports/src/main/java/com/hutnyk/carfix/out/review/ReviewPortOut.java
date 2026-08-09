package com.hutnyk.carfix.out.review;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.review.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewPortOut {

    boolean existsByBookingId(BookingId bookingId);

    Optional<Review> findByBookingId(BookingId bookingId);

    Review insert(Review review);

    /**
     * The branch the booking belongs to — the branch whose cached rating this review changes.
     * Empty when the booking does not exist.
     */
    Optional<BranchId> findBranchIdByBookingId(BookingId bookingId);

    /**
     * Every review's star count for one branch, unordered. The mean is computed in the
     * domain ({@code BranchRating}), not here — this port only fetches.
     */
    List<Integer> findStarsByBranchId(BranchId branchId);
}
