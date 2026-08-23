package com.hutnyk.carfix.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hutnyk.carfix.booking.exception.BookingCancellationNotAllowedException;
import com.hutnyk.carfix.booking.exception.BookingNoShowNotAllowedException;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.carProfile.CarProfileId;
import com.hutnyk.carfix.employee.EmployeeId;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.scheduling.SegmentPlan;
import com.hutnyk.carfix.scheduling.TimeRange;
import com.hutnyk.carfix.scheduling.VisitPlan;
import com.hutnyk.carfix.service.EmployeeRequirement;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.ServiceStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BookingTest {

    private static final BranchId BRANCH_ID = BranchId.genId();
    private static final EmployeeId ANNA = EmployeeId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final LocalDate BOOKED_DAY = LocalDate.of(2030, 6, 12);
    private static final LocalDateTime START = BOOKED_DAY.atTime(10, 0);
    private static final LocalDateTime END = BOOKED_DAY.atTime(11, 30);
    private static final LocalDateTime BEFORE_START = START.minusMinutes(1);

    private static Booking withStatus(BookingStatus status) {
        return Booking.of(
                BookingId.genId(),
                BOOKED_DAY,
                status,
                LocalTime.of(10, 0),
                LocalTime.of(11, 30),
                BranchId.genId(),
                CarProfileId.genId(),
                List.of()
        );
    }

    private static Service service(int id, int minutes, String price) {
        return Service.of(id, "Service " + id, null, (short) minutes, new BigDecimal(price),
                ServiceStatus.ACTIVE, BRANCH_ID, 1, Set.of(1),
                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(10))), List.of());
    }

    private static VisitPlan plan(LocalDate date) {
        SegmentPlan first = new SegmentPlan(11, TimeRange.of(date.atTime(9, 0), date.atTime(9, 50)),
                Map.of(1, ANNA), Map.of());
        SegmentPlan second = new SegmentPlan(27, TimeRange.of(date.atTime(10, 0), date.atTime(11, 30)),
                Map.of(1, ANNA), Map.of());
        return new VisitPlan(100, List.of(first, second));
    }

    @Test
    public void cancelFlipsScheduledToCancelledAndKeepsEverythingElse() {
        Booking scheduled = withStatus(BookingStatus.SCHEDULED);

        Booking cancelled = scheduled.cancel(BEFORE_START);

        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(cancelled.getId()).isEqualTo(scheduled.getId());
        assertThat(cancelled.getDate()).isEqualTo(scheduled.getDate());
        assertThat(cancelled.getStartTime()).isEqualTo(scheduled.getStartTime());
        assertThat(cancelled.getEndTime()).isEqualTo(scheduled.getEndTime());
        assertThat(cancelled.getBranchId()).isEqualTo(scheduled.getBranchId());
        assertThat(cancelled.getCarProfileId()).isEqualTo(scheduled.getCarProfileId());
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = {"CANCELLED", "NO_SHOW"})
    public void cancelRefusesAStoredFactWhateverTheClockSays(BookingStatus status) {
        Booking booking = withStatus(status);

        assertThatThrownBy(() -> booking.cancel(BEFORE_START))
                .isInstanceOf(BookingCancellationNotAllowedException.class)
                .hasMessageContaining(status.name());
    }

    @Test
    public void cancelRefusesABookingThatHasAlreadyStarted() {
        Booking booking = withStatus(BookingStatus.SCHEDULED);

        assertThatThrownBy(() -> booking.cancel(START))
                .isInstanceOf(BookingCancellationNotAllowedException.class)
                .hasMessageContaining(BookingStatus.IN_PROGRESS.name());
    }

    @Test
    public void cancelRefusesABookingThatHasAlreadyFinished() {
        Booking booking = withStatus(BookingStatus.SCHEDULED);

        assertThatThrownBy(() -> booking.cancel(END))
                .isInstanceOf(BookingCancellationNotAllowedException.class)
                .hasMessageContaining(BookingStatus.COMPLETED.name());
    }

    @Test
    public void effectiveStatusIsScheduledUntilTheInstantItStarts() {
        Booking booking = withStatus(BookingStatus.SCHEDULED);

        assertThat(booking.effectiveStatus(BEFORE_START)).isEqualTo(BookingStatus.SCHEDULED);
        assertThat(booking.effectiveStatus(START)).isEqualTo(BookingStatus.IN_PROGRESS);
    }

    @Test
    public void effectiveStatusIsInProgressUntilTheInstantItEnds() {
        Booking booking = withStatus(BookingStatus.SCHEDULED);

        assertThat(booking.effectiveStatus(END.minusMinutes(1))).isEqualTo(BookingStatus.IN_PROGRESS);
        assertThat(booking.effectiveStatus(END)).isEqualTo(BookingStatus.COMPLETED);
        assertThat(booking.effectiveStatus(END.plusYears(1))).isEqualTo(BookingStatus.COMPLETED);
    }

    @ParameterizedTest
    @EnumSource(value = BookingStatus.class, names = {"CANCELLED", "NO_SHOW"})
    public void effectiveStatusKeepsAStoredFactAtEveryInstant(BookingStatus stored) {
        Booking booking = withStatus(stored);

        assertThat(booking.effectiveStatus(BEFORE_START)).isEqualTo(stored);
        assertThat(booking.effectiveStatus(START)).isEqualTo(stored);
        assertThat(booking.effectiveStatus(END)).isEqualTo(stored);
    }

    @Test
    public void markNoShowIsAllowedFromTheSlotStartOnwards() {
        Booking booking = withStatus(BookingStatus.SCHEDULED);

        assertThat(booking.markNoShow(START).getStatus()).isEqualTo(BookingStatus.NO_SHOW);
        assertThat(booking.markNoShow(END).getStatus()).isEqualTo(BookingStatus.NO_SHOW);
        assertThat(booking.markNoShow(END.plusDays(3)).getStatus()).isEqualTo(BookingStatus.NO_SHOW);
    }

    @Test
    public void markNoShowRefusesABookingThatHasNotStarted() {
        Booking booking = withStatus(BookingStatus.SCHEDULED);

        assertThatThrownBy(() -> booking.markNoShow(BEFORE_START))
                .isInstanceOf(BookingNoShowNotAllowedException.class)
                .hasMessageContaining(BookingStatus.SCHEDULED.name());
    }

    @Test
    public void markNoShowRefusesACancelledBooking() {
        Booking booking = withStatus(BookingStatus.CANCELLED);

        assertThatThrownBy(() -> booking.markNoShow(END))
                .isInstanceOf(BookingNoShowNotAllowedException.class)
                .hasMessageContaining(BookingStatus.CANCELLED.name());
    }

    @Test
    public void markNoShowOfAnAlreadyMarkedBookingChangesNothing() {
        Booking booking = withStatus(BookingStatus.NO_SHOW);

        assertThat(booking.markNoShow(END)).isSameAs(booking);
    }

    @Test
    public void safeCancelUntilSubtractsTheGivenNoticeInBranchZone() {
        Booking booking = withStatus(BookingStatus.SCHEDULED);

        Instant deadline = booking.safeCancelUntil(ZoneId.of("Europe/Warsaw"), Duration.ofHours(24));
        Instant strict = booking.safeCancelUntil(ZoneId.of("Europe/Warsaw"), Duration.ofHours(48));
        Instant flexible = booking.safeCancelUntil(ZoneId.of("Europe/Warsaw"), Duration.ofHours(2));

        // 2030-06-11 10:00 Warsaw summer time (UTC+2) == 08:00 UTC
        assertThat(deadline).isEqualTo(Instant.parse("2030-06-11T08:00:00Z"));
        assertThat(Duration.between(strict, flexible)).isEqualTo(Duration.ofHours(46));
    }

    @Test
    public void scheduleDerivesSpanSegmentsAndPriceSnapshotsFromThePlan() {
        LocalDate date = LocalDate.of(2030, 6, 12);
        BookingId id = BookingId.genId();
        CarProfileId carProfileId = CarProfileId.genId();

        Booking booking = Booking.schedule(id, BRANCH_ID, carProfileId, plan(date),
                List.of(service(27, 90, "300.00"), service(11, 50, "120.00")), LocalDateTime.of(2030, 6, 12, 8, 0));

        assertThat(booking.getId()).isEqualTo(id);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.SCHEDULED);
        assertThat(booking.getDate()).isEqualTo(date);
        assertThat(booking.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(booking.getEndTime()).isEqualTo(LocalTime.of(11, 30));
        assertThat(booking.getBranchId()).isEqualTo(BRANCH_ID);
        assertThat(booking.getCarProfileId()).isEqualTo(carProfileId);
        assertThat(booking.getSegments()).containsExactly(
                BookingSegment.of(11, LocalTime.of(9, 0), LocalTime.of(9, 50), new BigDecimal("120.00")),
                BookingSegment.of(27, LocalTime.of(10, 0), LocalTime.of(11, 30), new BigDecimal("300.00")));
    }

    @Test
    public void scheduleRejectsAPlanWhoseServiceIsNotInTheChain() {
        assertThatThrownBy(() -> Booking.schedule(BookingId.genId(), BRANCH_ID, CarProfileId.genId(),
                plan(LocalDate.of(2030, 6, 12)), List.of(service(11, 50, "120.00")), LocalDateTime.of(2030, 6, 12, 8, 0)))
                .isInstanceOf(UnexpectedStateException.class);
    }

    @Test
    public void scheduleRejectsAStartInThePast() {
        assertThatThrownBy(() -> Booking.schedule(BookingId.genId(), BRANCH_ID, CarProfileId.genId(),
                plan(LocalDate.of(2020, 6, 12)), List.of(service(11, 50, "120.00"), service(27, 90, "300.00")), LocalDateTime.of(2030, 6, 12, 8, 0)))
                .isInstanceOf(DomainObjectValidationException.class);
    }

    @Test
    public void scheduleAcceptsAStartEqualToNow() {
        //given
        LocalDate date = LocalDate.of(2030, 6, 12);
        //when
        Booking booking = Booking.schedule(BookingId.genId(), BRANCH_ID, CarProfileId.genId(), plan(date),
                List.of(service(27, 90, "300.00"), service(11, 50, "120.00")), date.atTime(9, 0));
        //then
        assertThat(booking.getStartTime()).isEqualTo(LocalTime.of(9, 0));
    }

    @Test
    public void scheduleRejectsAStartOneMinuteBeforeNow() {
        //given
        LocalDate date = LocalDate.of(2030, 6, 12);
        //when //then
        assertThatThrownBy(() -> Booking.schedule(BookingId.genId(), BRANCH_ID, CarProfileId.genId(), plan(date),
                List.of(service(27, 90, "300.00"), service(11, 50, "120.00")), date.atTime(9, 1)))
                .isInstanceOf(DomainObjectValidationException.class);
    }

    @Test
    public void ofSortsSegmentsByStartTime() {
        BookingSegment late = BookingSegment.of(27, LocalTime.of(10, 0), LocalTime.of(11, 30), BigDecimal.TEN);
        BookingSegment early = BookingSegment.of(11, LocalTime.of(9, 0), LocalTime.of(9, 50), BigDecimal.TEN);

        Booking booking = Booking.of(BookingId.genId(), BOOKED_DAY, BookingStatus.SCHEDULED,
                LocalTime.of(9, 0), LocalTime.of(11, 30), BranchId.genId(), CarProfileId.genId(), List.of(late, early));

        assertThat(booking.getSegments()).containsExactly(early, late);
    }
}
