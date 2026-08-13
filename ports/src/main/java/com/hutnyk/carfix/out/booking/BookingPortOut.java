package com.hutnyk.carfix.out.booking;

import com.hutnyk.carfix.booking.Booking;
import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.in.booking.query.BookingView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingPortOut {
    List<BookingView> findAllViewsByCustomerId(UUID customerId);
    Optional<BookingView> findViewByIdAndCustomerId(UUID bookingId, UUID customerId);
    Optional<Booking> findByIdAndCustomerId(UUID bookingId, UUID customerId);
    Booking update(Booking booking);
    void freeOccupancy(BookingId bookingId);
}
