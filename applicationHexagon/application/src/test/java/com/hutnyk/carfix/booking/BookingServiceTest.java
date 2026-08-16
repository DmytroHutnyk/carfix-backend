package com.hutnyk.carfix.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.booking.exception.BookingCancellationNotAllowedException;
import com.hutnyk.carfix.booking.exception.BookingNotFoundException;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.carProfile.CarProfileId;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.customer.CustomerStatus;
import com.hutnyk.carfix.in.booking.query.BookingServiceView;
import com.hutnyk.carfix.in.booking.query.BookingView;
import com.hutnyk.carfix.out.booking.BookingNotificationPortOut;
import com.hutnyk.carfix.out.booking.BookingPortOut;
import com.hutnyk.carfix.out.customer.CustomerPortOut;
import com.hutnyk.carfix.user.PasswordHash;
import com.hutnyk.carfix.user.PhoneNumber;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.user.UserRole;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BookingServiceTest {

    private static final String EMAIL = "john@example.com";
    private static final UserId CUSTOMER_ID = UserId.genId();
    private static final UUID BOOKING_ID = UUID.randomUUID();

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

    private static final class StubCustomerPortOut implements CustomerPortOut {
        @Override
        public Customer insertCustomer(Customer c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Customer loadCustomerByUsername(String email) {
            return customer();
        }
    }

    private static Booking booking(BookingStatus status) {
        return Booking.of(
                BookingId.of(BOOKING_ID),
                LocalDate.of(2030, 6, 12),
                status,
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                BranchId.genId(),
                CarProfileId.genId());
    }

    private static BookingView view(BookingStatus status) {
        return new BookingView(
                BOOKING_ID,
                LocalDate.of(2030, 6, 12),
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                status,
                Instant.parse("2030-06-11T08:00:00Z"),
                UUID.randomUUID(),
                "SpeedCare Wola",
                "+48123456789",
                "wola@speedcare.pl",
                "Górczewska",
                "110",
                "Warszawa",
                UUID.randomUUID(),
                "Weekend Car",
                "BMW",
                "X5",
                "KR 67890",
                List.of(new BookingServiceView("Diagnostics", new BigDecimal("150.00"))),
                new BigDecimal("150.00"));
    }

    private static final class StubBookingPortOut implements BookingPortOut {
        Booking stored;
        Booking updated;
        UUID requestedCustomerId;

        @Override
        public List<BookingView> findAllViewsByCustomerId(UUID customerId) {
            this.requestedCustomerId = customerId;
            return List.of(view(BookingStatus.SCHEDULED));
        }

        @Override
        public Optional<BookingView> findViewByIdAndCustomerId(UUID bookingId, UUID customerId) {
            return stored == null ? Optional.empty() : Optional.of(view(BookingStatus.CANCELLED));
        }

        @Override
        public Optional<Booking> findByIdAndCustomerId(UUID bookingId, UUID customerId) {
            this.requestedCustomerId = customerId;
            return Optional.ofNullable(stored);
        }

        @Override
        public Booking update(Booking booking) {
            this.updated = booking;
            return booking;
        }
    }

    private static final class RecordingNotifier implements BookingNotificationPortOut {
        User confirmedTo;
        BookingView confirmed;
        User cancelledTo;
        BookingView cancelled;

        @Override
        public void sendBookingConfirmed(User customer, BookingView booking) {
            this.confirmedTo = customer;
            this.confirmed = booking;
        }

        @Override
        public void sendBookingCancelled(User customer, BookingView booking) {
            this.cancelledTo = customer;
            this.cancelled = booking;
        }
    }

    private final StubBookingPortOut bookingPortOut = new StubBookingPortOut();
    private final RecordingNotifier notifier = new RecordingNotifier();
    private final BookingService service = new BookingService(new StubCustomerPortOut(), bookingPortOut, notifier);

    @Test
    public void getMyBookingsResolvesCustomerAndReturnsViews() {
        List<BookingView> result = service.getMyBookings(EMAIL);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().branchName()).isEqualTo("SpeedCare Wola");
        assertThat(bookingPortOut.requestedCustomerId).isEqualTo(CUSTOMER_ID.id());
    }

    @Test
    public void cancelBookingPersistsCancelledStateAndReturnsFreshView() {
        bookingPortOut.stored = booking(BookingStatus.SCHEDULED);

        BookingView result = service.cancelBooking(EMAIL, BOOKING_ID);

        assertThat(bookingPortOut.updated.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(bookingPortOut.updated.getId().id()).isEqualTo(BOOKING_ID);
        assertThat(result.status()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    public void cancelBookingUnknownOrForeignIdThrowsNotFound() {
        bookingPortOut.stored = null;

        assertThatThrownBy(() -> service.cancelBooking(EMAIL, BOOKING_ID))
                .isInstanceOf(BookingNotFoundException.class);
        assertThat(bookingPortOut.updated).isNull();
    }

    @Test
    public void cancelBookingNonScheduledPropagatesDomainRefusalWithoutPersisting() {
        bookingPortOut.stored = booking(BookingStatus.COMPLETED);

        assertThatThrownBy(() -> service.cancelBooking(EMAIL, BOOKING_ID))
                .isInstanceOf(BookingCancellationNotAllowedException.class);
        assertThat(bookingPortOut.updated).isNull();
    }

    @Test
    public void cancelBookingEmailsTheCustomerWithTheFreshCancelledView() {
        bookingPortOut.stored = booking(BookingStatus.SCHEDULED);

        BookingView result = service.cancelBooking(EMAIL, BOOKING_ID);

        assertThat(notifier.cancelledTo.getEmail()).isEqualTo(EMAIL);
        assertThat(notifier.cancelledTo.getId()).isEqualTo(CUSTOMER_ID);
        assertThat(notifier.cancelled).isSameAs(result);
        assertThat(notifier.cancelled.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(notifier.confirmed).isNull();
    }

    @Test
    public void cancelBookingOfUnknownIdSendsNoEmail() {
        bookingPortOut.stored = null;

        assertThatThrownBy(() -> service.cancelBooking(EMAIL, BOOKING_ID))
                .isInstanceOf(BookingNotFoundException.class);
        assertThat(notifier.cancelled).isNull();
    }

    @Test
    public void cancelBookingRefusedByTheDomainSendsNoEmail() {
        bookingPortOut.stored = booking(BookingStatus.COMPLETED);

        assertThatThrownBy(() -> service.cancelBooking(EMAIL, BOOKING_ID))
                .isInstanceOf(BookingCancellationNotAllowedException.class);
        assertThat(notifier.cancelled).isNull();
    }
}
