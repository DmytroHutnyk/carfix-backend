package com.hutnyk.carfix;

import com.hutnyk.carfix.util.Validator;
import lombok.*;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Address {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String buildingNumber;
    //Nullable
    private final String flatNumber;
    private final Long streetId;
    private final Integer postalCodeId;

    @Builder
    private Address(Integer id, String buildingNumber, String flatNumber, Long streetId, Integer postalCodeId) {
        this.id = Validator.notNull(id);
        this.buildingNumber = Validator.notEmpty(buildingNumber);
        this.flatNumber = validFlatNumber(flatNumber);
        this.streetId = Validator.notNull(streetId);
        this.postalCodeId = Validator.notNull(postalCodeId);
    }

    public Address of(Integer id, String buildingNumber, String flatNumber, Long streetId, Integer postalCodeId){
        return Address.builder()
                .id(id)
                .buildingNumber(buildingNumber)
                .flatNumber(flatNumber)
                .streetId(streetId)
                .postalCodeId(postalCodeId)
                .build();
    }

    private static String validFlatNumber(String flatNumber){
        if(flatNumber == null){
            return null;
        }

        if(flatNumber.isBlank()){
            throw new InvalidDomainObjectError("Flat number can not be empty or blank");
        }
        return flatNumber;
    }
}
