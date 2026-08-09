package com.hutnyk.carfix.serviceBay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.scheduling.TimeRange;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class ServiceBayBookingTest {

    private static final LocalDate DATE = LocalDate.of(2026, 8, 12);
    private static final TimeRange RANGE = TimeRange.of(
            LocalDateTime.of(2026, 8, 12, 14, 0), LocalDateTime.of(2026, 8, 12, 15, 0));
    private static final BookingId BOOKING_ID = BookingId.of(UUID.randomUUID());

    @Test
    public void test_of_builds_occupancy_row() {
        //when
        ServiceBayBooking result = ServiceBayBooking.of(1, RANGE, DATE, 3, BOOKING_ID);

        //then
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getBookedTime()).isEqualTo(RANGE);
        assertThat(result.getDate()).isEqualTo(DATE);
        assertThat(result.getServiceBayId()).isEqualTo(3);
        assertThat(result.getBookingId()).isEqualTo(BOOKING_ID);
    }

    @Test
    public void test_of_throws_when_booking_id_is_null() {
        //when + then
        assertThatThrownBy(() -> ServiceBayBooking.of(1, RANGE, DATE, 3, null))
                .isInstanceOf(DomainObjectValidationException.class)
                .extracting("errorType")
                .isEqualTo(ValidationErrorType.NULL_VALUE);
    }
}
