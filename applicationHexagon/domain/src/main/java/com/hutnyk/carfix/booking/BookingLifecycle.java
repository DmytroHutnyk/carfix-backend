package com.hutnyk.carfix.booking;

import java.time.LocalDateTime;

// Stored: SCHEDULED, CANCELLED, NO_SHOW. IN_PROGRESS and COMPLETED are derived.
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
