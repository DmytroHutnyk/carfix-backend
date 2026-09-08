package com.hutnyk.carfix.openingHours;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class OpeningHoursException {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime closeTime;
    private final Boolean isOpen;
    //Nullable
    private final String reason;
    private final BranchId branchId;

    @Builder
    private OpeningHoursException(
            Integer id,
            LocalDate date,
            LocalTime startTime,
            LocalTime closeTime,
            Boolean isOpen,
            String reason,
            BranchId branchId) {
        this.id = id;
        this.date = Validator.notNull(date, "date");
        this.isOpen = Validator.notNull(isOpen, "isOpen");
        requireHoursWhenOpen(this.isOpen, startTime, closeTime);
        this.startTime = startTime;
        this.closeTime = closeTime;
        this.reason = reason;
        this.branchId = Validator.notNull(branchId, "branchId");
    }

    public static OpeningHoursException of(
            Integer id,
            LocalDate date,
            LocalTime startTime,
            LocalTime closeTime,
            Boolean isOpen,
            String reason,
            BranchId branchId) {
        return OpeningHoursException.builder()
                .id(id)
                .date(date)
                .startTime(startTime)
                .closeTime(closeTime)
                .isOpen(isOpen)
                .reason(reason)
                .branchId(branchId)
                .build();
    }

    private static void requireHoursWhenOpen(boolean isOpen, LocalTime startTime, LocalTime closeTime) {
        if (isOpen) {
            Validator.notNull(startTime, "startTime");
            Validator.notNull(closeTime, "closeTime");
        }
    }
}
