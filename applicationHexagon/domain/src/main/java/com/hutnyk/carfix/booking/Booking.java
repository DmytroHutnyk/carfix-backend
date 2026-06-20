package com.hutnyk.carfix.booking;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.carProfile.CarProfileId;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.time.OffsetTime;

import static com.hutnyk.carfix.util.Validator.futureOrPresent;
import static com.hutnyk.carfix.util.Validator.notNull;
import static com.hutnyk.carfix.util.Validator.validTimeRange;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Booking {

    @EqualsAndHashCode.Include
    private final BookingId id;
    private final LocalDate date;
    private final BookingStatus status;
    private final OffsetTime startTime;
    private final OffsetTime endTime;
    private final BranchId branchId;
    private final CarProfileId carProfileId;

    @Builder
    private Booking(
            BookingId id,
            LocalDate date,
            BookingStatus status,
            OffsetTime startTime,
            OffsetTime endTime,
            BranchId branchId,
            CarProfileId carProfileId) {
        this.id = notNull(id, "id");
        this.date = futureOrPresent(date, "date");
        this.status = notNull(status, "status");
        validTimeRange(startTime, endTime, "time");
        this.startTime = startTime;
        this.endTime = endTime;
        this.branchId = notNull(branchId, "branchId");
        this.carProfileId = notNull(carProfileId, "carProfileId");
    }

    public static Booking of(
            BookingId id,
            LocalDate date,
            BookingStatus status,
            OffsetTime startTime,
            OffsetTime endTime,
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
}
