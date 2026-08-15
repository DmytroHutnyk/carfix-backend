package com.hutnyk.carfix.booking;

import com.hutnyk.carfix.booking.exception.BookingCancellationNotAllowedException;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.carProfile.CarProfileId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.With;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.hutnyk.carfix.util.Validator.*;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Booking {

    public static final Duration SAFE_CANCELLATION_NOTICE = Duration.ofHours(24);

    @EqualsAndHashCode.Include
    private final BookingId id;
    private final LocalDate date;
    @With(AccessLevel.PRIVATE)
    private final BookingStatus status;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final BranchId branchId;
    private final CarProfileId carProfileId;

    @Builder
    private Booking(
            BookingId id,
            LocalDate date,
            BookingStatus status,
            LocalTime startTime,
            LocalTime endTime,
            BranchId branchId,
            CarProfileId carProfileId) {
        this.id = notNull(id, "id");
        this.date = notNull(date, "date");
        this.status = notNull(status, "status");
        validTimeRange(startTime, endTime, "time");
        this.startTime = startTime;
        this.endTime = endTime;
        this.branchId = notNull(branchId, "branchId");
        this.carProfileId = notNull(carProfileId, "carProfileId");
    }

    /**
     * Assembles an existing booking from persistence (no creation-time checks).
     */
    public static Booking of(
            BookingId id,
            LocalDate date,
            BookingStatus status,
            LocalTime startTime,
            LocalTime endTime,
            BranchId branchId,
            CarProfileId carProfileId) {
        return Booking.builder()
                .id(id)
                .date(date)
                .status(status)
                .startTime(startTime)
                .endTime(endTime)
                .branchId(branchId)
                .carProfileId(carProfileId)
                .build();
    }

    /**
     * Creates a brand-new booking (validates the slot is not in the past, in the branch's zone).
     */
    public static Booking schedule(
            BookingId id,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            BranchId branchId,
            CarProfileId carProfileId,
            ZoneId branchZone) {
        notInPast(date, startTime, branchZone, "date");
        return Booking.builder()
                .id(id)
                .date(date)
                .status(BookingStatus.SCHEDULED)
                .startTime(startTime)
                .endTime(endTime)
                .branchId(branchId)
                .carProfileId(carProfileId)
                .build();
    }

    public Booking cancel() {
        if (status != BookingStatus.SCHEDULED) {
            throw new BookingCancellationNotAllowedException(id.id(), status);
        }
        return withStatus(BookingStatus.CANCELLED);
    }

    public Instant safeCancelUntil(ZoneId branchZone) {
        return ZonedDateTime.of(date, startTime, branchZone)
                .minus(SAFE_CANCELLATION_NOTICE)
                .toInstant();
    }
}
