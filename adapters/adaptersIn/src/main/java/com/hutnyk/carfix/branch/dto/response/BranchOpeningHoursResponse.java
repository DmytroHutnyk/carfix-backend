package com.hutnyk.carfix.branch.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hutnyk.carfix.openingHours.DayOfWeek;

import java.time.LocalTime;

public record BranchOpeningHoursResponse(
        DayOfWeek dayOfWeek,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime startTime,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime closeTime
) {}
