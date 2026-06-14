package com.hutnyk.carfix.address;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.util.Validator;
import lombok.*;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Address {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String streetName;
    private final String buildingNumber;
    //Nullable
    private final String flatNumber;
    private final String postalCode;
    private final Integer cityId;


    @Builder
    private Address(
            Integer id,
            String streetName,
            String buildingNumber,
            String flatNumber,
            String postalCode,
            Integer cityId) {
        this.id = id;
        this.streetName = Validator.notBlank(streetName, "streetName");
        this.buildingNumber = Validator.notBlank(buildingNumber, "buildingNumber");
        this.flatNumber = validateFlatNumber(flatNumber);
        this.postalCode = Validator.notBlank(postalCode, "postalCode"); //TODO add regex validation per country
        this.cityId = Validator.notNull(cityId, "cityId");
    }

    public static Address of(
            Integer id,
            String streetName,
            String buildingNumber,
            String flatNumber,
            String postalCode,
            Integer cityId){
        return Address.builder()
                .id(id)
                .streetName(streetName)
                .buildingNumber(buildingNumber)
                .flatNumber(flatNumber)
                .postalCode(postalCode)
                .cityId(cityId)
                .build();
    }

    private static String validateFlatNumber(String flatNumber){
        if(flatNumber == null){
            return null;
        }

        if(flatNumber.isBlank()){
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_FLAT_NUMBER, "flatNumber", flatNumber);
        }
        return flatNumber;
    }
}
