package com.hutnyk.carfix.in.search.query;

import java.time.LocalDate;
import java.time.LocalTime;

public record AvailabilityWindow(
        LocalDate from,
        LocalDate to,
        LocalTime timeFrom,
        LocalTime timeTo
) {}
