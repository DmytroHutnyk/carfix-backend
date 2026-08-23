package com.hutnyk.carfix.address;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class City {

    private static final BigDecimal MAX_ABS_LATITUDE = new BigDecimal("90");
    private static final BigDecimal MAX_ABS_LONGITUDE = new BigDecimal("180");

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;
    private final Integer regionId;
    //Nullable
    private final BigDecimal latitude;
    //Nullable
    private final BigDecimal longitude;

    @Builder
    private City(Integer id, String name, Integer regionId, BigDecimal latitude, BigDecimal longitude) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
        this.regionId = Validator.notNull(regionId, "regionId");
        validateCoordinates(latitude, longitude);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static City of(Integer id, String name, Integer regionId, BigDecimal latitude, BigDecimal longitude){
        return City.builder()
                .id(id)
                .name(name)
                .regionId(regionId)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    public boolean hasCoordinates() {
        return latitude != null;
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
