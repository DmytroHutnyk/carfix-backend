package com.hutnyk.carfix.user.dto.response;

import com.hutnyk.carfix.address.CountryIso;

import java.math.BigDecimal;

public record LocationResponse(
        String city,
        String region,
        CountryIso countryIso,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
