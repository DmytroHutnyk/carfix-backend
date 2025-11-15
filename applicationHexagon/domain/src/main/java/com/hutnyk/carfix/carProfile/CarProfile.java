package com.hutnyk.carfix.carProfile;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.user.UserId;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;
import java.util.regex.Pattern;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class CarProfile {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;

    //Nullable
    private final String vin;

    //Nullable
    private final String plates;

    //Nullable
    private final LocalDate serviceCertificateValidUpTo;

    //Nullable
    private final LocalDate insuranceValidUpTo;
    private final UserId customerId;

    //Nullable
    private final Integer fileId;
    private final Integer modelGenerationId;

    @Builder
    private CarProfile(
            Integer id,
            String name,
            String vin,
            String plates,
            LocalDate serviceCertificateValidUpTo,
            LocalDate insuranceValidUpTo,
            UserId customerId,
            Integer fileId,
            Integer modelGenerationId) {
        this.id = Validator.notNull(id, "id");
        this.name = Validator.notBlank(name, "name");
        this.vin = validateVin(vin);
        this.plates = validatePlates(plates);
        this.serviceCertificateValidUpTo = validateDate(serviceCertificateValidUpTo, "serviceCertificateValidUpTo");
        this.insuranceValidUpTo = validateDate(insuranceValidUpTo, "insuranceValidUpTo");
        this.customerId = Validator.notNull(customerId, "customerId");
        this.fileId = fileId;
        this.modelGenerationId = Validator.notNull(modelGenerationId, "modelGenerationId");
    }

    public static CarProfile of(
            Integer id,
            String name,
            String vin,
            String plates,
            LocalDate serviceCertificateValidUpTo,
            LocalDate insuranceValidUpTo,
            UserId customerId,
            Integer fileId,
            Integer modelGenerationId) {
        return CarProfile.builder()
                .id(id)
                .name(name)
                .vin(vin)
                .plates(plates)
                .serviceCertificateValidUpTo(serviceCertificateValidUpTo)
                .insuranceValidUpTo(insuranceValidUpTo)
                .customerId(customerId)
                .fileId(fileId)
                .modelGenerationId(modelGenerationId)
                .build();
    }

    private static String validateVin(String value){
        if(value == null){
            return null;
        }

        if(!Pattern.matches("^[A-HJ-NPR-Z0-9]{17}$", value)){
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_VIN_FORMAT, "vin", value);
        }
        return value;
    }

    private static String validatePlates(String value){
        if(value == null){
            return null;
        }

        if(!Pattern.matches("^(?=.{5,10}$)(?=.*[A-Z])(?=.*\\d)[A-Z0-9](?:[ -]?[A-Z0-9])+$", value)){
            throw new DomainObjectValidationException(ValidationErrorType.INVALID_PLATES_FORMAT, "plates", value);
        }
        return value;
    }

    private static LocalDate validateDate(LocalDate date, String fieldName){
        if(date == null){
            return null;
        }

        if(date.isBefore(LocalDate.now().minusYears(20))){
            throw new DomainObjectValidationException(ValidationErrorType.EXPIRATION_DATE_TOO_OLD, fieldName, date);
        }

        return date;
    }
}
