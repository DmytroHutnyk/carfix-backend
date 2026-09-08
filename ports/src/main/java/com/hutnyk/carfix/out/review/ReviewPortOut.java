package com.hutnyk.carfix.out.review;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.in.branch.query.BranchReviewsPage;
import com.hutnyk.carfix.in.branch.query.BranchReviewsQuery;
import com.hutnyk.carfix.review.Review;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewPortOut {

    boolean existsByBookingId(BookingId bookingId);

    void deleteByCustomerId(UUID customerId);

    Optional<Review> findByBookingId(BookingId bookingId);

    Review insert(Review review);

    Optional<BranchId> findBranchIdByBookingId(BookingId bookingId);

    // Return raw stars; BranchRating owns averaging.
    List<Integer> findStarsByBranchId(BranchId branchId);

    BranchReviewsPage findReviewsPage(BranchReviewsQuery query);
}
