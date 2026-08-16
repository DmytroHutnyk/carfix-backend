package com.hutnyk.carfix.branch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.branch.exception.InvalidReviewsSortException;
import com.hutnyk.carfix.in.branch.query.BranchReviewsPage;
import com.hutnyk.carfix.in.branch.query.BranchReviewsQuery;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.out.branch.BranchPortOut;
import com.hutnyk.carfix.out.review.ReviewPortOut;
import com.hutnyk.carfix.review.BranchRating;
import com.hutnyk.carfix.review.Review;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class BranchServiceTest {

    private static final UUID BRANCH_ID = UUID.randomUUID();

    private static BranchView view() {
        return new BranchView(
                BRANCH_ID, "AutoFix Mokotow", "+48221234567", "contact@autofix.pl",
                "A workshop.", "Free cancellation up to 24 hours.",
                new BigDecimal("4.7"), 236,
                "Pulawska", "45", "Warsaw",
                new BigDecimal("52.180000"), new BigDecimal("21.020000"), null, "Europe/Warsaw",
                List.of(), List.of(), List.of());
    }

    private static final class StubBranchPortOut implements BranchPortOut {
        BranchView view;
        boolean branchExists = true;

        @Override
        public void updateRating(BranchId branchId, BranchRating rating) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<BranchView> findViewById(BranchId branchId) {
            return Optional.ofNullable(view);
        }

        @Override
        public boolean existsActiveById(BranchId branchId) {
            return branchExists;
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

    private static final class StubReviewPortOut implements ReviewPortOut {
        BranchReviewsQuery receivedQuery;

        @Override
        public boolean existsByBookingId(BookingId bookingId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Review> findByBookingId(BookingId bookingId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Review insert(Review review) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<BranchId> findBranchIdByBookingId(BookingId bookingId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Integer> findStarsByBranchId(BranchId branchId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BranchReviewsPage findReviewsPage(BranchReviewsQuery query) {
            this.receivedQuery = query;
            return BranchReviewsPage.empty(query.page(), query.size());
        }
    }

    private final StubBranchPortOut branchStub = new StubBranchPortOut();
    private final StubReviewPortOut reviewStub = new StubReviewPortOut();
    private final BranchService service = new BranchService(branchStub, reviewStub);

    @Test
    public void test_getBranch_returns_view() {
        //given
        branchStub.view = view();

        //when
        BranchView result = service.getBranch(BRANCH_ID);

        //then
        assertThat(result.name()).isEqualTo("AutoFix Mokotow");
    }

    @Test
    public void test_getBranch_unknown_id_throws_not_found() {
        //given
        branchStub.view = null;

        //when + then
        assertThatThrownBy(() -> service.getBranch(BRANCH_ID))
                .isInstanceOf(BranchNotFoundException.class);
    }

    @Test
    public void test_getReviews_defaults_null_sort_to_newest() {
        //when
        service.getReviews(new BranchReviewsQuery(BRANCH_ID, null, 0, 10));

        //then
        assertThat(reviewStub.receivedQuery.sort()).isEqualTo(BranchReviewsQuery.SORT_NEWEST);
    }

    @Test
    public void test_getReviews_normalizes_sort_case() {
        //when
        service.getReviews(new BranchReviewsQuery(BRANCH_ID, "HIGHEST", 0, 10));

        //then
        assertThat(reviewStub.receivedQuery.sort()).isEqualTo(BranchReviewsQuery.SORT_HIGHEST);
    }

    @Test
    public void test_getReviews_unknown_sort_throws_before_the_port() {
        //when + then
        assertThatThrownBy(() -> service.getReviews(new BranchReviewsQuery(BRANCH_ID, "bogus", 0, 10)))
                .isInstanceOf(InvalidReviewsSortException.class);
        assertThat(reviewStub.receivedQuery).isNull();
    }

    @Test
    public void test_getReviews_unknown_branch_throws_not_found() {
        //given
        branchStub.branchExists = false;

        //when + then
        assertThatThrownBy(() -> service.getReviews(new BranchReviewsQuery(BRANCH_ID, null, 0, 10)))
                .isInstanceOf(BranchNotFoundException.class);
        assertThat(reviewStub.receivedQuery).isNull();
    }

    @Test
    public void test_getReviews_caps_size_at_50() {
        //when
        service.getReviews(new BranchReviewsQuery(BRANCH_ID, null, 0, 99));

        //then
        assertThat(reviewStub.receivedQuery.size()).isEqualTo(50);
    }
}
