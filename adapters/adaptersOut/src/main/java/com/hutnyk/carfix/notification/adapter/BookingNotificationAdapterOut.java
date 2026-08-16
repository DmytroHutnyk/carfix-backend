package com.hutnyk.carfix.notification.adapter;

import com.hutnyk.carfix.components.NotificationAdapter;
import com.hutnyk.carfix.in.booking.query.BookingView;
import com.hutnyk.carfix.notification.mail.EmailSender;
import com.hutnyk.carfix.notification.mapper.BookingEmailMapper;
import com.hutnyk.carfix.out.booking.BookingNotificationPortOut;
import com.hutnyk.carfix.user.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@NotificationAdapter
public class BookingNotificationAdapterOut implements BookingNotificationPortOut {

    private final EmailSender emailSender;

    @Override
    public void sendBookingConfirmed(User customer, BookingView booking) {
        emailSender.send(BookingEmailMapper.confirmed(customer, booking));
    }

    @Override
    public void sendBookingCancelled(User customer, BookingView booking) {
        emailSender.send(BookingEmailMapper.cancelled(customer, booking));
    }
}
