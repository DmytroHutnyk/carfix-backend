package com.hutnyk.carfix.booking.exception;

import com.hutnyk.carfix.carProfile.CarProfileId;
import com.hutnyk.carfix.exception.ConflictException;

import java.time.LocalDate;
import java.time.LocalTime;

/** The same car already has an active booking overlapping the requested visit — one car, one place at a time. */
public class CarProfileAlreadyBookedException extends ConflictException {

    public CarProfileAlreadyBookedException(CarProfileId carProfileId, LocalDate date, LocalTime start, LocalTime end) {
        super(BookingErrorCode.CAR_PROFILE_ALREADY_BOOKED,
                "This car already has a booking overlapping " + date + " " + start + "-" + end,
                "carProfileId", carProfileId.id());
    }
}
