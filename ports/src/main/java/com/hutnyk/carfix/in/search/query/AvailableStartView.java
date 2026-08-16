package com.hutnyk.carfix.in.search.query;

import java.time.LocalDate;
import java.time.LocalTime;

public record AvailableStartView(
        LocalDate date,
        LocalTime startTime
) {}
