package com.hutnyk.carfix.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.branch.Branch;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.in.review.commands.AddReviewCommand;
import com.hutnyk.carfix.in.branch.query.BranchReviewsPage;
import com.hutnyk.carfix.in.branch.query.BranchReviewsQuery;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.out.branch.BranchPortOut;
import com.hutnyk.carfix.out.review.ReviewPortOut;
import com.hutnyk.carfix.review.exception.ReviewAlreadyExistsException;
import com.hutnyk.carfix.review.exception.ReviewedBookingNotFoundException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ReviewServiceTest {

    private static final BookingId BOOKING_ID = BookingId.genId();
    private static final BranchId BRANCH_ID = BranchId.genId();

    private static final class StubReviewPortOut implements ReviewPortOut {
        boolean exists = false;
        Optional<BranchId> branchId = Optional.of(BRANCH_ID);
        final List<Integer> stars = new ArrayList<>(List.of(5, 4));
        Review inserted;

        @Override
        public boolean existsByBookingId(BookingId bookingId) {
            return exists;
        }

        @Override
        public Optional<Review> findByBookingId(BookingId bookingId) {
            return Optional.empty();
        }

        @Override
        public Review insert(Review review) {
            this.inserted = review;
            /* The service must read the stars AFTER the insert, or the new review is
               missing from the average it just wrote. */
            stars.add(review.getStarsNumber());
            return review;
        }

        @Override
        public Optional<BranchId> findBranchIdByBookingId(BookingId bookingId) {
            return branchId;
        }

        @Override
        public List<Integer> findStarsByBranchId(BranchId branchId) {
            return List.copyOf(stars);
        }

        @Override
        public BranchReviewsPage findReviewsPage(BranchReviewsQuery query) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class StubBranchPortOut implements BranchPortOut {
        BranchId receivedBranchId;
        BranchRating receivedRating;

        @Override
        public void updateRating(BranchId branchId, BranchRating rating) {
            this.receivedBranchId = branchId;
            this.receivedRating = rating;
        }

        @Override
        public Optional<BranchView> findViewById(BranchId branchId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsActiveById(BranchId branchId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<ZoneId> findActiveBranchZone(BranchId branchId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Branch insert(Branch branch) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insertOpeningHours(List<OpeningHours> openingHours) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void linkCarBrands(BranchId branchId, Set<Integer> carBrandIds) {
            throw new UnsupportedOperationException();
        }
    }

    private static AddReviewCommand command(int stars) {
        return new AddReviewCommand(BOOKING_ID, stars, "Solid work");
    }

    @Test
    void adding_a_review_stores_it() {
        //given
        StubReviewPortOut reviewPort = new StubReviewPortOut();
        ReviewService service = new ReviewService(reviewPort, new StubBranchPortOut());

        //when
        Review saved = service.addReview(command(3));

        //then
        assertThat(reviewPort.inserted).isNotNull();
        assertThat(saved.getStarsNumber()).isEqualTo(3);
        assertThat(saved.getBookingId()).isEqualTo(BOOKING_ID);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void adding_a_review_refreshes_the_branch_rating_including_the_new_review() {
        //given — the branch already has 5 and 4; adding a 3 must give (5+4+3)/3 = 4.0
        StubReviewPortOut reviewPort = new StubReviewPortOut();
        StubBranchPortOut branchPort = new StubBranchPortOut();
        ReviewService service = new ReviewService(reviewPort, branchPort);

        //when
        service.addReview(command(3));

        //then
        assertThat(branchPort.receivedBranchId).isEqualTo(BRANCH_ID);
        assertThat(branchPort.receivedRating.count()).isEqualTo(3);
        assertThat(branchPort.receivedRating.average()).isEqualByComparingTo(new BigDecimal("4.0"));
    }

    @Test
    void a_booking_can_only_be_reviewed_once() {
        //given
        StubReviewPortOut reviewPort = new StubReviewPortOut();
        reviewPort.exists = true;
        StubBranchPortOut branchPort = new StubBranchPortOut();
        ReviewService service = new ReviewService(reviewPort, branchPort);

        //when / then
        assertThatThrownBy(() -> service.addReview(command(4)))
                .isInstanceOf(ReviewAlreadyExistsException.class);
        assertThat(reviewPort.inserted).isNull();
        assertThat(branchPort.receivedRating).isNull();
    }

    @Test
    void reviewing_an_unknown_booking_is_rejected() {
        //given
        StubReviewPortOut reviewPort = new StubReviewPortOut();
        reviewPort.branchId = Optional.empty();
        StubBranchPortOut branchPort = new StubBranchPortOut();
        ReviewService service = new ReviewService(reviewPort, branchPort);

        //when / then
        assertThatThrownBy(() -> service.addReview(command(4)))
                .isInstanceOf(ReviewedBookingNotFoundException.class);
        assertThat(reviewPort.inserted).isNull();
    }
}
