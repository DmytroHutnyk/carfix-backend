package com.hutnyk.carfix.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.booking.exception.BookingCancellationNotAllowedException;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.carProfile.CarProfileId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

public class BookingTest {

    private static Booking withStatus(BookingStatus status) {
        return Booking.of(
                BookingId.genId(),
                LocalDate.of(2030, 6, 12),
                status,
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                BranchId.genId(),
                CarProfileId.genId()
        );
    }

    @Test
    public void cancelFlipsScheduledToCancelledAndKeepsEverythingElse() {
        Booking scheduled = withStatus(BookingStatus.SCHEDULED);

        Booking cancelled = scheduled.cancel();

        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(cancelled.getId()).isEqualTo(scheduled.getId());
        assertThat(cancelled.getDate()).isEqualTo(scheduled.getDate());
        assertThat(cancelled.getStartTime()).isEqualTo(scheduled.getStartTime());
        assertThat(cancelled.getEndTime()).isEqualTo(scheduled.getEndTime());
        assertThat(cancelled.getBranchId()).isEqualTo(scheduled.getBranchId());
        assertThat(cancelled.getCarProfileId()).isEqualTo(scheduled.getCarProfileId());
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = {"IN_PROGRESS", "COMPLETED", "CANCELLED"})
    public void cancelRefusesEveryNonScheduledStatus(BookingStatus status) {
        Booking booking = withStatus(status);

        assertThatThrownBy(booking::cancel)
                .isInstanceOf(BookingCancellationNotAllowedException.class)
                .hasMessageContaining(status.name());
    }

    @Test
    public void safeCancelUntilIsTwentyFourHoursBeforeStartInBranchZone() {
        Booking booking = withStatus(BookingStatus.SCHEDULED);

        Instant deadline = booking.safeCancelUntil(ZoneId.of("Europe/Warsaw"));

        // 2030-06-11 10:00 Warsaw summer time (UTC+2) == 08:00 UTC
        assertThat(deadline).isEqualTo(Instant.parse("2030-06-11T08:00:00Z"));
    }
}
