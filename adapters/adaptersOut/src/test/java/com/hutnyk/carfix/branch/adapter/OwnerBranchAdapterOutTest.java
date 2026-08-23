package com.hutnyk.carfix.branch.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.booking.BookingStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class OwnerBranchAdapterOutTest {

    private static final UUID BRANCH = UUID.randomUUID();
    private static final UUID OTHER_BRANCH = UUID.randomUUID();
    private static final LocalDate TODAY = LocalDate.of(2030, 6, 12);
    private static final LocalDateTime NOON = TODAY.atTime(12, 0);

    private static final Predicate<BookingStatus> BOOKED =
            status -> status != BookingStatus.CANCELLED && status != BookingStatus.NO_SHOW;
    private static final Predicate<BookingStatus> COMPLETED = status -> status == BookingStatus.COMPLETED;

    private static Object[] row(UUID branchId, LocalDate date, BookingStatus status,
                                LocalTime start, LocalTime end, long count) {
        return new Object[]{branchId, date, status, start, end, count};
    }

    private static final List<Object[]> ROWS = List.of(
            row(BRANCH, TODAY, BookingStatus.SCHEDULED, LocalTime.of(8, 0), LocalTime.of(9, 0), 2L),
            row(BRANCH, TODAY, BookingStatus.SCHEDULED, LocalTime.of(11, 30), LocalTime.of(12, 30), 1L),
            row(BRANCH, TODAY, BookingStatus.SCHEDULED, LocalTime.of(15, 0), LocalTime.of(16, 0), 3L),
            row(BRANCH, TODAY, BookingStatus.CANCELLED, LocalTime.of(8, 0), LocalTime.of(9, 0), 4L),
            row(BRANCH, TODAY, BookingStatus.NO_SHOW, LocalTime.of(8, 0), LocalTime.of(9, 0), 5L),
            row(OTHER_BRANCH, TODAY, BookingStatus.SCHEDULED, LocalTime.of(8, 0), LocalTime.of(9, 0), 7L),
            row(BRANCH, TODAY.minusDays(1), BookingStatus.SCHEDULED, LocalTime.of(8, 0), LocalTime.of(9, 0), 9L));

    @Test
    public void bookingsTodayCountsEverythingButCancelledAndNoShows() {
        assertThat(OwnerBranchAdapterOut.sumBookings(ROWS, BRANCH, NOON, BOOKED)).isEqualTo(6);
    }

    @Test
    public void completedCountsOnlySlotsThatHaveAlreadyEnded() {
        assertThat(OwnerBranchAdapterOut.sumBookings(ROWS, BRANCH, NOON, COMPLETED)).isEqualTo(2);
    }

    @Test
    public void completedGrowsAsTheBranchDayPasses() {
        assertThat(OwnerBranchAdapterOut.sumBookings(ROWS, BRANCH, TODAY.atTime(7, 0), COMPLETED)).isZero();
        assertThat(OwnerBranchAdapterOut.sumBookings(ROWS, BRANCH, TODAY.atTime(23, 0), COMPLETED)).isEqualTo(6);
    }

    @Test
    public void aFinishedSlotThatWasCancelledOrMarkedNoShowNeverCountsAsCompleted() {
        List<Object[]> rows = List.of(
                row(BRANCH, TODAY, BookingStatus.CANCELLED, LocalTime.of(8, 0), LocalTime.of(9, 0), 1L),
                row(BRANCH, TODAY, BookingStatus.NO_SHOW, LocalTime.of(8, 0), LocalTime.of(9, 0), 1L));

        assertThat(OwnerBranchAdapterOut.sumBookings(rows, BRANCH, NOON, COMPLETED)).isZero();
    }
}
