package com.hutnyk.carfix.booking;

import java.math.BigDecimal;
import java.util.List;

/**
 * Pricing rules for a booking.
 */
public final class BookingPricing {

    private BookingPricing() {
    }

    public static BigDecimal total(List<BigDecimal> servicePrices) {
        return servicePrices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
