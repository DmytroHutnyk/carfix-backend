package com.hutnyk.carfix.branch.dto.request;

import com.hutnyk.carfix.openingHours.DayOfWeek;
import com.hutnyk.carfix.openingHours.OpeningHoursMode;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record RegisterBranchOpeningHoursRequest(
        @NotNull DayOfWeek dayOfWeek,
        @NotNull LocalTime opensAt,
        @NotNull LocalTime closesAt,
        @NotNull OpeningHoursMode mode
) {}
