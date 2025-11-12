package com.hutnyk.carfix.util;

import com.hutnyk.carfix.InvalidDomainObjectError;

import java.util.Set;

public class IsoValidator {
    public static final Set<String> COUNTRY_CODES = Set.of(
            "US", "CA", "GB", "DE", "FR", "IT", "ES", "PL", "UA", "PT",
            "NL", "SE", "NO", "FI", "DK", "CH", "AT", "BE", "CZ", "SK",
            "HU", "RO", "BG", "GR", "TR", "IE", "IS", "AU", "NZ", "JP",
            "CN", "KR", "IN", "BR", "AR", "MX", "ZA", "EG", "IL", "SA", "AE"
    );

    public static String validateCountryIso(String iso){
        Validator.notEmpty(iso);

        if(!COUNTRY_CODES.contains(iso)){
            throw new InvalidDomainObjectError("Iso code is not valid");
        }

        return iso;
    }
}
