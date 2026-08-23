package com.hutnyk.carfix.user.dto.response;

import com.hutnyk.carfix.address.CountryIso;

import java.math.BigDecimal;

public record AddressResponse(
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
