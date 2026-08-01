package com.hutnyk.carfix.booking.dto.response;

import java.math.BigDecimal;

public record BookingServiceResponse(
        String name,
        BigDecimal price
) {}
