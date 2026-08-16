package com.hutnyk.carfix.notification.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.booking.BookingStatus;
import com.hutnyk.carfix.in.booking.query.BookingServiceView;
import com.hutnyk.carfix.in.booking.query.BookingView;
import com.hutnyk.carfix.notification.mail.EmailMessage;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookingNotificationAdapterOutTest {

    private static final User CUSTOMER = User.builder()
            .id(UserId.genId())
            .name("John")
            .surname("Doe")
            .phoneNumber(new PhoneNumber("+48", "123456789"))
            .email("john@example.com")
            .role(UserRole.CUSTOMER)
            .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
            .dateOfBirth(null)
            .addressId(null)
            .emailVerifiedAt(null)
            .build();

    private static BookingView view(BookingStatus status) {
        return new BookingView(
                UUID.randomUUID(),
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

    private final List<EmailMessage> sent = new ArrayList<>();
    private final BookingNotificationAdapterOut adapter = new BookingNotificationAdapterOut(sent::add);

    @Test
    public void test_sendBookingConfirmed_sends_the_confirmation_to_the_customer() {
        //when
        adapter.sendBookingConfirmed(CUSTOMER, view(BookingStatus.SCHEDULED));
        //then
        assertThat(sent).hasSize(1);
        assertThat(sent.getFirst().to()).isEqualTo("john@example.com");
        assertThat(sent.getFirst().subject()).startsWith("Booking confirmed · SpeedCare Wola");
    }

    @Test
    public void test_sendBookingCancelled_sends_the_cancellation_to_the_customer() {
        //when
        adapter.sendBookingCancelled(CUSTOMER, view(BookingStatus.CANCELLED));
        //then
        assertThat(sent).hasSize(1);
        assertThat(sent.getFirst().to()).isEqualTo("john@example.com");
        assertThat(sent.getFirst().subject()).startsWith("Booking cancelled · SpeedCare Wola");
    }
}
