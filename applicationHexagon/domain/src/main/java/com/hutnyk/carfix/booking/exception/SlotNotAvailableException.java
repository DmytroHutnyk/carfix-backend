package com.hutnyk.carfix.booking.exception;

import com.hutnyk.carfix.exception.ConflictException;

import java.time.LocalDate;
import java.time.LocalTime;

/** No feasible assignment remains, or this transaction lost a resource race. Client refetches slots. */
public class SlotNotAvailableException extends ConflictException {

    public SlotNotAvailableException(LocalDate date, LocalTime startTime) {
        super(BookingErrorCode.SLOT_NOT_AVAILABLE,
                "Slot " + date + " " + startTime + " is no longer available",
                null, null);
    }
}
