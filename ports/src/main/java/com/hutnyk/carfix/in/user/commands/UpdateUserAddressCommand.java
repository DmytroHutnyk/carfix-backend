package com.hutnyk.carfix.in.user.commands;

import java.math.BigDecimal;

public record UpdateUserAddressCommand(
        String streetName,
        String buildingNumber,
        String flatNumber,
        String postalCode,
        String city,
        String region,
        String countryIso,
        BigDecimal latitude,
        BigDecimal longitude,
        String googlePlaceId
) {
}
