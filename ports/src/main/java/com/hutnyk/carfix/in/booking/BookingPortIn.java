package com.hutnyk.carfix.in.booking;

import com.hutnyk.carfix.in.booking.commands.CreateBookingCommand;
import com.hutnyk.carfix.in.booking.query.BookingView;

import java.util.List;
import java.util.UUID;

public interface BookingPortIn {
    List<BookingView> getMyBookings(String customerEmail);
    BookingView createBooking(String customerEmail, CreateBookingCommand command);
    BookingView cancelBooking(String customerEmail, UUID bookingId);
}
