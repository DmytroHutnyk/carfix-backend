package com.hutnyk.carfix.in.booking;

import java.util.UUID;

public interface OwnerBookingPortIn {

    /** Records that the customer never turned up; idempotent once the booking already says so. */
    void markNoShow(String ownerEmail, UUID bookingId);
}
