package com.hutnyk.carfix.booking.exception;

import com.hutnyk.carfix.exception.ConflictException;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * The requested start cannot be booked: no feasible assignment now exists (recompute found none),
 * or a concurrent booking won the GiST exclusion race (SQLSTATE 23P01). The client refetches slots.
 */
public class SlotNotAvailableException extends ConflictException {

    public SlotNotAvailableException(LocalDate date, LocalTime startTime) {
        super(BookingErrorCode.SLOT_NOT_AVAILABLE,
                "Slot " + date + " " + startTime + " is no longer available",
                null, null);
    }
}
