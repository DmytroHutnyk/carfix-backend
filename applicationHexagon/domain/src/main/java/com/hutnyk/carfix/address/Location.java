package com.hutnyk.carfix.address;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.util.Validator;

import java.math.BigDecimal;

/**
 * A place at city / region / country level with its geocoded centre — what a user browses workshops
 * around. Not a postal address ({@link Address}); city and region may be absent for a coarser pick.
 */
public record Location(
        //Nullable
        String city,
        //Nullable
        String region,
        CountryIso countryIso,
        //Nullable
        BigDecimal latitude,
        //Nullable
        BigDecimal longitude) {

    private static final BigDecimal MAX_ABS_LATITUDE = new BigDecimal("90");
    private static final BigDecimal MAX_ABS_LONGITUDE = new BigDecimal("180");

    public Location {
        Validator.notNull(countryIso, "countryIso");
        validateOptionalName(city, "city");
        validateOptionalName(region, "region");
        validateCoordinates(latitude, longitude);
    }

    private static void validateOptionalName(String value, String fieldName) {
        if (value != null && value.isBlank()) {
            throw new DomainObjectValidationException(ValidationErrorType.EMPTY_STRING, fieldName, value);
        }
    }

    private static void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null && longitude == null) {
            return;
        }
        Validator.notNull(latitude, "latitude");
        Validator.notNull(longitude, "longitude");
        if (latitude.abs().compareTo(MAX_ABS_LATITUDE) > 0) {
            throw new DomainObjectValidationException(ValidationErrorType.VALUE_OUT_OF_RANGE, "latitude", latitude);
        }
        if (longitude.abs().compareTo(MAX_ABS_LONGITUDE) > 0) {
            throw new DomainObjectValidationException(ValidationErrorType.VALUE_OUT_OF_RANGE, "longitude", longitude);
        }
    }
}
