package com.hutnyk.carfix.in.branch.query;

import com.hutnyk.carfix.openingHours.DayOfWeek;

import java.time.LocalTime;

public record BranchOpeningHoursView(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime closeTime
) {}
