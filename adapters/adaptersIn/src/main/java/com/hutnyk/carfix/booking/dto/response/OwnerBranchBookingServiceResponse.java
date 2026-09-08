package com.hutnyk.carfix.booking.dto.response;

import java.math.BigDecimal;

public record OwnerBranchBookingServiceResponse(
        String name,
        int durationMinutes,
        BigDecimal price
) {}
