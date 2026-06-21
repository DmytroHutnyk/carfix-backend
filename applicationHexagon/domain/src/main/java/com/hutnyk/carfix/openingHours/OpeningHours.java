package com.hutnyk.carfix.openingHours;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalTime;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class OpeningHours {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final DayOfWeek dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime closeTime;
    private final BranchId branchId;

    @Builder
    private OpeningHours(
            Integer id,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime closeTime,
            BranchId branchId) {
        this.id = id;
        this.dayOfWeek = Validator.notNull(dayOfWeek, "dayOfWeek");
        this.startTime = Validator.notNull(startTime, "startTime");
        this.closeTime = Validator.notNull(closeTime, "closeTime");
        this.branchId = Validator.notNull(branchId, "branchId");
    }

    public static OpeningHours of(
            Integer id,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime closeTime,
            BranchId branchId) {
        return OpeningHours.builder()
                .id(id)
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .closeTime(closeTime)
                .branchId(branchId)
                .build();
    }
}
