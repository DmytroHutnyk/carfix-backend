package com.hutnyk.carfix.in.search.query;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Requested availability filter; all fields nullable, all-null means no filter.
 */
public record AvailabilityWindow(
        LocalDate from,
        LocalDate to,
        LocalTime timeFrom,
        LocalTime timeTo
) {}
