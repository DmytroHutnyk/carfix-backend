package com.hutnyk.carfix.in.booking;

import java.util.UUID;

public interface OwnerBookingPortIn {

    void markNoShow(String ownerEmail, UUID bookingId);
}
