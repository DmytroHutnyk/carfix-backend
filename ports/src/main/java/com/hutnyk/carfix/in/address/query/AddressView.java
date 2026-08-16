package com.hutnyk.carfix.in.address.query;

import com.hutnyk.carfix.address.CountryIso;

import java.math.BigDecimal;

public record AddressView(
        Integer id,
        String streetName,
        String buildingNumber,
        String flatNumber,
        String postalCode,
        String city,
        String region,
        CountryIso countryIso,
        String countryName,
        BigDecimal latitude,
        BigDecimal longitude,
        String googlePlaceId
) {
}
