package com.hutnyk.carfix.out.booking;

import com.hutnyk.carfix.in.booking.query.BookingView;
import com.hutnyk.carfix.user.User;

// Delivery starts after commit and never fails the originating use case.
public interface BookingNotificationPortOut {

    void sendBookingConfirmed(User customer, BookingView booking);

    void sendBookingCancelled(User customer, BookingView booking);
}
