package com.hutnyk.carfix.address;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.util.Validator;

/**
 * ISO 3166-1 alpha-2 country codes.
 * These enum constants match the ISO codes (e.g., US, CA, GB).
 */
public enum CountryIso {
    US,
    CA,
    GB,
    DE,
    FR,
    IT,
    ES,
    PL,
    UA,
    PT,
    NL,
    SE,
    NO,
    FI,
    DK,
    CH,
    AT,
    BE,
    CZ,
    SK,
    HU,
    RO,
    BG,
    GR,
    TR,
    IE,
    IS,
    AU,
    NZ,
    JP,
    CN,
    KR,
    IN,
    BR,
    AR,
    MX,
    ZA,
    EG,
    IL,
    SA,
    AE;

    public static CountryIso parse(String code) {
        Validator.notBlank(code, "countryIso");
        try {
            return CountryIso.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_ISO_CODE, "countryIso", code,
                    "Country " + code + " is not supported");
        }
    }
}

