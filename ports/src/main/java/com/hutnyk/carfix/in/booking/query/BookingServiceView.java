package com.hutnyk.carfix.in.booking.query;

import java.math.BigDecimal;

public record BookingServiceView(
        String name,
        BigDecimal price
) {}
