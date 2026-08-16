package com.hutnyk.carfix.in.user.commands;

import java.math.BigDecimal;

public record LocationCommand(
        String city,
        String region,
        String countryIso,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
