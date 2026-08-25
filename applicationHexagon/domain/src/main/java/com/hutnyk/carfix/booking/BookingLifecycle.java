package com.hutnyk.carfix.booking;

import java.time.LocalDateTime;

/**
 * Where a booking stands at a given instant. Only {@code CANCELLED} and {@code NO_SHOW} are ever
 * stored; every other status is time passing, so it is derived instead of written.
 */
public final class BookingLifecycle {

    private BookingLifecycle() {
    }

    public static BookingStatus effectiveStatus(
            BookingStatus stored, LocalDateTime start, LocalDateTime end, LocalDateTime now) {
        if (stored == BookingStatus.CANCELLED || stored == BookingStatus.NO_SHOW) {
            return stored;
        }
        if (now.isBefore(start)) {
            return BookingStatus.SCHEDULED;
        }
        if (now.isBefore(end)) {
            return BookingStatus.IN_PROGRESS;
        }
        return BookingStatus.COMPLETED;
    }
}
