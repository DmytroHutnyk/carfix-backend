package com.hutnyk.carfix.in.branch.commands;

import java.math.BigDecimal;

public record RegisterBranchAddressCommand(
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
