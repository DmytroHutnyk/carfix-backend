package com.hutnyk.carfix.in.booking.query;

import java.math.BigDecimal;

public record OwnerBranchBookingServiceView(
        String name,
        int durationMinutes,
        BigDecimal price
) {}
