package com.hutnyk.carfix.branch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.branch.exception.InvalidReviewsSortException;
import com.hutnyk.carfix.in.branch.query.BranchReviewsPage;
import com.hutnyk.carfix.in.branch.query.BranchReviewsQuery;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;
import com.hutnyk.carfix.out.branch.BranchPortOut;
import com.hutnyk.carfix.out.branch.OwnerBranchPortOut;
import com.hutnyk.carfix.out.review.ReviewPortOut;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.review.BranchRating;
import com.hutnyk.carfix.review.Review;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BranchServiceTest {

    private static final UUID BRANCH_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-08-17T08:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final String OWNER_EMAIL = "owner@carfix.dev";
    private static final UserId OWNER_ID = UserId.genId();

    private static BranchView view() {
        return new BranchView(
                BRANCH_ID, "AutoFix Mokotow", "+48221234567", "contact@autofix.pl",
                "A workshop.", "Free cancellation up to 24 hours.",
                new BigDecimal("4.7"), 236,
                "Pulawska", "45", "Warsaw",
                new BigDecimal("52.180000"), new BigDecimal("21.020000"), null, "Europe/Warsaw",
                List.of(), List.of(), List.of());
    }

    private static User owner() {
        return User.builder()
                .id(OWNER_ID)
                .name("Marek")
                .surname("Kowalski")
                .phoneNumber(new PhoneNumber("+48", "600100200"))
                .email(OWNER_EMAIL)
                .role(UserRole.OWNER)
                .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                .build();
    }

    private static OwnerBranchSummaryView summary() {
        return new OwnerBranchSummaryView(
                BRANCH_ID, "AutoFix Mokotow", BranchStatus.ACTIVE,
                "Pulawska", "45", "Warsaw",
                new BigDecimal("4.7"), 236,
                true, 12, 7, 3, 5,
                List.of());
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
        public Optional<ZoneId> findActiveBranchZone(BranchId branchId) {
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

    private static final class StubOwnerBranchPortOut implements OwnerBranchPortOut {
        UserId receivedOwnerId;
        Instant receivedNow;

        @Override
        public List<OwnerBranchSummaryView> findSummariesByOwnerId(UserId ownerId, Instant now) {
            this.receivedOwnerId = ownerId;
            this.receivedNow = now;
            return List.of(summary());
        }
    }

    private static final class StubUserPortOut implements UserPortOut {
        User user;

        @Override
        public Optional<User> loadUserByEmail(String email) {
            return Optional.ofNullable(user);
        }

        @Override
        public boolean existsByEmail(String email) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByPhoneNumber(PhoneNumber phoneNumber) {
            throw new UnsupportedOperationException();
        }

        @Override
        public User update(User user) {
            throw new UnsupportedOperationException();
        }
    }

    private final StubBranchPortOut branchStub = new StubBranchPortOut();
    private final StubReviewPortOut reviewStub = new StubReviewPortOut();
    private final StubOwnerBranchPortOut ownerBranchStub = new StubOwnerBranchPortOut();
    private final StubUserPortOut userStub = new StubUserPortOut();
    private final BranchService service =
            new BranchService(branchStub, reviewStub, ownerBranchStub, userStub, FIXED_CLOCK);

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

    @Test
    public void test_getMyBranchSummaries_resolves_owner_and_passes_clock_instant() {
        //given
        userStub.user = owner();

        //when
        List<OwnerBranchSummaryView> result = service.getMyBranchSummaries(OWNER_EMAIL);

        //then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("AutoFix Mokotow");
        assertThat(ownerBranchStub.receivedOwnerId).isEqualTo(OWNER_ID);
        assertThat(ownerBranchStub.receivedNow).isEqualTo(NOW);
    }

    @Test
    public void test_getMyBranchSummaries_unknown_principal_throws_before_the_port() {
        //given
        userStub.user = null;

        //when + then
        assertThatThrownBy(() -> service.getMyBranchSummaries(OWNER_EMAIL))
                .isInstanceOf(AuthenticatedUserMissingException.class);
        assertThat(ownerBranchStub.receivedOwnerId).isNull();
    }
}
