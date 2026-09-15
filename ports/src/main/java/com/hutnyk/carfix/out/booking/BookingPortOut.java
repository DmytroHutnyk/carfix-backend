package com.hutnyk.carfix.out.booking;

import com.hutnyk.carfix.booking.Booking;
import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.BookingOccupancy;
import com.hutnyk.carfix.carProfile.CarProfileId;
import com.hutnyk.carfix.in.booking.query.BookingView;
import com.hutnyk.carfix.in.booking.query.OwnerBranchBookingView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingPortOut {
    List<BookingView> findAllViewsByCustomerId(UUID customerId);
    Optional<BookingView> findViewByIdAndCustomerId(UUID bookingId, UUID customerId);
    Optional<Booking> findByIdAndCustomerId(UUID bookingId, UUID customerId);
    List<OwnerBranchBookingView> findBranchBookings(UUID branchId, LocalDate from, LocalDate to);

    Optional<Booking> findByIdAndOwnerId(UUID bookingId, UUID ownerId);

    void insert(Booking booking, BookingOccupancy occupancy);

    Booking update(Booking booking);
    void freeOccupancy(BookingId bookingId);
    void deleteAllByCustomerId(UUID customerId);

    boolean existsActiveOverlapping(CarProfileId carProfileId, LocalDate date, LocalTime start, LocalTime end);
}
