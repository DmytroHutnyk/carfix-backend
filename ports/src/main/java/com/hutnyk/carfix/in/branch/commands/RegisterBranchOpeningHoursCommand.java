package com.hutnyk.carfix.in.branch.commands;

import com.hutnyk.carfix.openingHours.DayOfWeek;
import com.hutnyk.carfix.openingHours.OpeningHoursMode;

import java.time.LocalTime;

public record RegisterBranchOpeningHoursCommand(
        DayOfWeek dayOfWeek,
        LocalTime opensAt,
        LocalTime closesAt,
        OpeningHoursMode mode
) {
}
