package com.hutnyk.carfix.in.address.query;

import com.hutnyk.carfix.address.CountryIso;

import java.math.BigDecimal;

public record LocationView(
        Integer cityId,
        String city,
        String region,
        CountryIso countryIso,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
