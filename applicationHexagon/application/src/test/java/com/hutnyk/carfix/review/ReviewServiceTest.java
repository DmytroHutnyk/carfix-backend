package com.hutnyk.carfix.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.booking.Booking;
import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.BookingOccupancy;
import com.hutnyk.carfix.booking.BookingStatus;
import com.hutnyk.carfix.branch.Branch;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.carProfile.CarProfileId;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.customer.CustomerStatus;
import com.hutnyk.carfix.in.booking.query.BookingView;
import com.hutnyk.carfix.in.review.commands.AddReviewCommand;
import com.hutnyk.carfix.in.branch.query.BranchReviewsPage;
import com.hutnyk.carfix.in.branch.query.BranchReviewsQuery;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.OpeningHoursException;
import com.hutnyk.carfix.out.booking.BookingPortOut;
import com.hutnyk.carfix.out.branch.BranchPortOut;
import com.hutnyk.carfix.out.customer.CustomerPortOut;
import com.hutnyk.carfix.out.review.ReviewPortOut;
import com.hutnyk.carfix.review.exception.ReviewAlreadyExistsException;
import com.hutnyk.carfix.review.exception.ReviewNotAllowedException;
import com.hutnyk.carfix.review.exception.ReviewedBookingNotFoundException;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ReviewServiceTest {

    private static final String EMAIL = "john@example.com";
    private static final BookingId BOOKING_ID = BookingId.genId();
    private static final BranchId BRANCH_ID = BranchId.genId();
    private static final UserId CUSTOMER_ID = UserId.genId();
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-16T10:00:00Z"), ZoneOffset.UTC);

    private static Booking bookingOn(LocalDate date) {
        return Booking.of(BOOKING_ID, date, BookingStatus.SCHEDULED,
                LocalTime.of(9, 0), LocalTime.of(10, 0), BRANCH_ID, CarProfileId.genId(), List.of());
    }

    private static Customer customer() {
        return Customer.of(
                User.builder()
                        .id(CUSTOMER_ID)
                        .name("John")
                        .surname("Doe")
                        .phoneNumber(new PhoneNumber("+48", "123456789"))
                        .email(EMAIL)
                        .role(UserRole.CUSTOMER)
                        .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                        .dateOfBirth(LocalDate.of(1990, 5, 1))
                        .addressId(null)
                        .build(),
                CustomerStatus.ACTIVE);
    }

    private static AddReviewCommand command(int stars) {
        return new AddReviewCommand(BOOKING_ID, stars, "Solid work");
    }

    private static final class StubReviewPortOut implements ReviewPortOut {
        boolean exists = false;
        final List<Integer> stars = new ArrayList<>(List.of(5, 4));
        Review inserted;

        @Override
        public boolean existsByBookingId(BookingId bookingId) {
            return exists;
        }

        @Override
        public void deleteByCustomerId(UUID customerId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Review> findByBookingId(BookingId bookingId) {
            return Optional.empty();
        }

        @Override
        public Review insert(Review review) {
            this.inserted = review;
            stars.add(review.getStarsNumber());
            return review;
        }

        @Override
        public Optional<BranchId> findBranchIdByBookingId(BookingId bookingId) {
            return Optional.of(BRANCH_ID);
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

    private static final class StubBookingPortOut implements BookingPortOut {
        Optional<Booking> found = Optional.of(bookingOn(LocalDate.of(2020, 1, 10)));
        UUID receivedCustomerId;

        @Override
        public Optional<Booking> findByIdAndCustomerId(UUID bookingId, UUID customerId) {
            this.receivedCustomerId = customerId;
            return found;
        }

        @Override
        public List<BookingView> findAllViewsByCustomerId(UUID customerId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<BookingView> findViewByIdAndCustomerId(UUID bookingId, UUID customerId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Booking> findByIdAndOwnerId(UUID bookingId, UUID ownerId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insert(Booking booking, BookingOccupancy occupancy) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Booking update(Booking booking) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void freeOccupancy(BookingId bookingId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteAllByCustomerId(UUID customerId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsActiveOverlapping(CarProfileId carProfileId, LocalDate date, LocalTime start, LocalTime end) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<com.hutnyk.carfix.in.booking.query.OwnerBranchBookingView> findBranchDayBookings(UUID branchId, LocalDate date) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class StubCustomerPortOut implements CustomerPortOut {
        @Override
        public Customer insertCustomer(Customer customer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Customer loadCustomerByUsername(String email) {
            return customer();
        }

        @Override
        public void deleteByUserId(UUID userId) {
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
        public Optional<ZoneId> findActiveBranchZone(BranchId branchId) {
            return Optional.of(ZoneId.of("Europe/Warsaw"));
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

        @Override
        public Branch update(Branch branch) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void replaceOpeningHours(BranchId branchId, List<OpeningHours> openingHours) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void replaceOpeningHoursExceptions(BranchId branchId, List<OpeningHoursException> exceptions) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void replaceCarBrands(BranchId branchId, Set<Integer> carBrandIds) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByIdAndOwnerId(BranchId branchId, java.util.UUID ownerId) {
            throw new UnsupportedOperationException();
        }
    }

    private final StubReviewPortOut reviewPort = new StubReviewPortOut();
    private final StubBranchPortOut branchPort = new StubBranchPortOut();
    private final StubBookingPortOut bookingPort = new StubBookingPortOut();
    private final StubCustomerPortOut customerPort = new StubCustomerPortOut();
    private final ReviewService service = new ReviewService(reviewPort, branchPort, bookingPort, customerPort, CLOCK);

    @Test
    void adding_a_review_to_a_completed_booking_stores_it() {
        //when
        Review saved = service.addReview(EMAIL, command(3));

        //then
        assertThat(reviewPort.inserted).isNotNull();
        assertThat(saved.getStarsNumber()).isEqualTo(3);
        assertThat(saved.getBookingId()).isEqualTo(BOOKING_ID);
        assertThat(saved.getId()).isNotNull();
        assertThat(bookingPort.receivedCustomerId).isEqualTo(CUSTOMER_ID.id());
    }

    @Test
    void adding_a_review_refreshes_the_branch_rating_including_the_new_review() {
        //given — the branch already has 5 and 4; adding a 3 must give (5+4+3)/3 = 4.0
        //when
        service.addReview(EMAIL, command(3));

        //then
        assertThat(branchPort.receivedBranchId).isEqualTo(BRANCH_ID);
        assertThat(branchPort.receivedRating.count()).isEqualTo(3);
        assertThat(branchPort.receivedRating.average()).isEqualByComparingTo(new BigDecimal("4.0"));
    }

    @Test
    void a_booking_can_only_be_reviewed_once() {
        //given
        reviewPort.exists = true;

        //when / then
        assertThatThrownBy(() -> service.addReview(EMAIL, command(4)))
                .isInstanceOf(ReviewAlreadyExistsException.class);
        assertThat(reviewPort.inserted).isNull();
        assertThat(branchPort.receivedRating).isNull();
    }

    @Test
    void reviewing_a_booking_that_is_not_the_customers_is_not_found() {
        //given
        bookingPort.found = Optional.empty();

        //when / then
        assertThatThrownBy(() -> service.addReview(EMAIL, command(4)))
                .isInstanceOf(ReviewedBookingNotFoundException.class);
        assertThat(reviewPort.inserted).isNull();
    }

    @Test
    void reviewing_a_booking_that_is_not_completed_is_rejected() {
        //given
        bookingPort.found = Optional.of(bookingOn(LocalDate.of(2030, 1, 10)));

        //when / then
        assertThatThrownBy(() -> service.addReview(EMAIL, command(4)))
                .isInstanceOf(ReviewNotAllowedException.class);
        assertThat(reviewPort.inserted).isNull();
        assertThat(branchPort.receivedRating).isNull();
    }
}
