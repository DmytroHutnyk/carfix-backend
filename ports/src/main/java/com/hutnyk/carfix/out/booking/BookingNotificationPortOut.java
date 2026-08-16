package com.hutnyk.carfix.out.booking;

import com.hutnyk.carfix.in.booking.query.BookingView;
import com.hutnyk.carfix.user.User;

/**
 * Outbound messages about a customer's booking. Fire-and-forget: implementations deliver after the current
 * transaction commits and never throw back into the use case.
 */
public interface BookingNotificationPortOut {

    void sendBookingConfirmed(User customer, BookingView booking);

    void sendBookingCancelled(User customer, BookingView booking);
}
