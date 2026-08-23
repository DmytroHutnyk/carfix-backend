package com.hutnyk.carfix.out.booking;

import com.hutnyk.carfix.booking.Booking;
import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.BookingOccupancy;
import com.hutnyk.carfix.carProfile.CarProfileId;
import com.hutnyk.carfix.in.booking.query.BookingView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingPortOut {
    List<BookingView> findAllViewsByCustomerId(UUID customerId);
    Optional<BookingView> findViewByIdAndCustomerId(UUID bookingId, UUID customerId);
    Optional<Booking> findByIdAndCustomerId(UUID bookingId, UUID customerId);

    /**
     * Persists the booking with its segments and the occupancy rows in the current transaction.
     * Throws {@link com.hutnyk.carfix.booking.exception.SlotNotAvailableException} when a GiST
     * exclusion constraint rejects an occupancy row (SQLSTATE 23P01) — a concurrent booking won.
     */
    void insert(Booking booking, BookingOccupancy occupancy);

    Booking update(Booking booking);
    void freeOccupancy(BookingId bookingId);

    /** True when the car profile already holds a SCHEDULED or IN_PROGRESS booking on that date overlapping [start, end). */
    boolean existsActiveOverlapping(CarProfileId carProfileId, LocalDate date, LocalTime start, LocalTime end);
}
