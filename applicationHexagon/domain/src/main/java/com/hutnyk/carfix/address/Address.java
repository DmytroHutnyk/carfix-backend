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
    private final String buildingNumber;
    //Nullable
    private final String flatNumber;
    private final Long streetId;
    private final Integer postalCodeId;

    @Builder
    private Address(
            Integer id,
            String buildingNumber,
            String flatNumber,
            Long streetId,
            Integer postalCodeId) {
        this.id = Validator.notNull(id, "id");
        this.buildingNumber = Validator.notBlank(buildingNumber, "buildingNumber");
        this.flatNumber = validateFlatNumber(flatNumber);
        this.streetId = Validator.notNull(streetId, "streetId");
        this.postalCodeId = Validator.notNull(postalCodeId, "postalCodeId");
    }

    public static Address of(
            Integer id,
            String buildingNumber,
            String flatNumber,
            Long streetId,
            Integer postalCodeId){
        return Address.builder()
                .id(id)
                .buildingNumber(buildingNumber)
                .flatNumber(flatNumber)
                .streetId(streetId)
                .postalCodeId(postalCodeId)
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
