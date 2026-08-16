package com.hutnyk.carfix.notification.mapper;

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
import java.util.List;
import java.util.UUID;

public class BookingEmailMapperTest {

    private static final UUID BOOKING_ID = UUID.fromString("1a2b3c4d-0000-4000-8000-000000000001");

    private static User customer(String name) {
        return User.builder()
                .id(UserId.genId())
                .name(name)
                .surname("Doe")
                .phoneNumber(new PhoneNumber("+48", "123456789"))
                .email("john@example.com")
                .role(UserRole.CUSTOMER)
                .passwordHash(PasswordHash.of("$2a$10$storedhashvalue"))
                .dateOfBirth(null)
                .addressId(null)
                .emailVerifiedAt(null)
                .build();
    }

    private static BookingView view(BookingStatus status, String plates, List<BookingServiceView> services, BigDecimal total) {
        return new BookingView(
                BOOKING_ID,
                LocalDate.of(2030, 6, 12),
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                status,
                Instant.parse("2030-06-11T08:00:00Z"),
                UUID.randomUUID(),
                "SpeedCare <Wola>",
                "+48123456789",
                "wola@speedcare.pl",
                "Górczewska",
                "110",
                "Warszawa",
                UUID.randomUUID(),
                "Weekend Car",
                "BMW",
                "X5",
                plates,
                services,
                total);
    }

    private static BookingView view() {
        return view(BookingStatus.SCHEDULED, "KR 67890",
                List.of(new BookingServiceView("Diagnostics", new BigDecimal("150.00")),
                        new BookingServiceView("Oil change", new BigDecimal("199.50"))),
                new BigDecimal("349.50"));
    }

    @Test
    public void test_confirmed_email_carries_subject_recipient_and_every_detail_in_the_text_body() {
        //when
        EmailMessage message = BookingEmailMapper.confirmed(customer("John"), view());
        //then
        assertThat(message.to()).isEqualTo("john@example.com");
        assertThat(message.subject()).isEqualTo("Booking confirmed · SpeedCare <Wola> · Wed, Jun 12, 2030 10:00");
        assertThat(message.textBody())
                .contains("Hi John,")
                .contains("Your booking is confirmed. Here are the details:")
                .contains("BK-1A2B3C4D")
                .contains("SpeedCare <Wola>")
                .contains("Górczewska 110, Warszawa")
                .contains("+48123456789")
                .contains("Wed, Jun 12, 2030")
                .contains("10:00–11:30")
                .contains("Weekend Car (BMW X5, KR 67890)")
                .contains("Diagnostics — 150 PLN")
                .contains("Oil change — 199.50 PLN")
                .contains("Total: 349.50 PLN")
                .contains("Cancelling later than 24 hours before the visit may result in a penalty.")
                .contains("CarFix");
    }

    @Test
    public void test_cancelled_email_uses_the_cancellation_wording() {
        //when
        EmailMessage message = BookingEmailMapper.cancelled(customer("John"), view());
        //then
        assertThat(message.subject()).isEqualTo("Booking cancelled · SpeedCare <Wola> · Wed, Jun 12, 2030 10:00");
        assertThat(message.textBody())
                .contains("Your booking has been cancelled. For your records:")
                .contains("BK-1A2B3C4D")
                .contains("If this was a mistake, you can book a new visit any time.")
                .doesNotContain("may result in a penalty");
    }

    @Test
    public void test_html_body_is_a_full_document_with_escaped_values() {
        //when
        EmailMessage message = BookingEmailMapper.confirmed(customer("<b>Bob</b>"), view());
        //then
        assertThat(message.htmlBody())
                .startsWith("<!doctype html>")
                .contains("Hi &lt;b&gt;Bob&lt;/b&gt;,")
                .contains("SpeedCare &lt;Wola&gt;")
                .contains("<li>Diagnostics — 150 PLN</li>")
                .contains("<li>Oil change — 199.50 PLN</li>")
                .contains("Total: 349.50 PLN")
                .doesNotContain("<b>Bob</b>")
                .doesNotContain("SpeedCare <Wola>");
    }

    @Test
    public void test_vehicle_without_plates_omits_the_plates_part() {
        //given
        BookingView noPlates = view(BookingStatus.SCHEDULED, null,
                List.of(new BookingServiceView("Diagnostics", new BigDecimal("150.00"))),
                new BigDecimal("150.00"));
        //when
        EmailMessage message = BookingEmailMapper.confirmed(customer("John"), noPlates);
        //then
        assertThat(message.textBody()).contains("Weekend Car (BMW X5)").contains("Total: 150 PLN");
    }
}
