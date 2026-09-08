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

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class CarProfile {

    @EqualsAndHashCode.Include
    private final CarProfileId id;
    private final String name;

    //Nullable
    private final String vin;

    //Nullable
    private final String plates;

    //Nullable
    private final LocalDate serviceCertificateDate;

    //Nullable
    private final LocalDate insuranceDate;
    private final UserId customerId;

    //Nullable
    private final Integer fileId;
    private final Integer modelVersionId;

    @Builder
    private CarProfile(
            CarProfileId id,
            String name,
            String vin,
            String plates,
            LocalDate serviceCertificateDate,
            LocalDate insuranceDate,
            UserId customerId,
            Integer fileId,
            Integer modelVersionId) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
        this.vin = validateVin(vin);
        this.plates = validatePlates(plates);
        this.serviceCertificateDate = serviceCertificateDate;
        this.insuranceDate = insuranceDate;
        this.customerId = Validator.notNull(customerId, "customerId");
        this.fileId = fileId;
        this.modelVersionId = Validator.notNull(modelVersionId, "modelVersionId");
    }

    // Rehydration trusts stored dates but still enforces structural invariants.
    public static CarProfile of(
            CarProfileId id,
            String name,
            String vin,
            String plates,
            LocalDate serviceCertificateDate,
            LocalDate insuranceDate,
            UserId customerId,
            Integer fileId,
            Integer modelVersionId) {
        return CarProfile.builder()
                .id(id)
                .name(name)
                .vin(vin)
                .plates(plates)
                .serviceCertificateDate(serviceCertificateDate)
                .insuranceDate(insuranceDate)
                .customerId(customerId)
                .fileId(fileId)
                .modelVersionId(modelVersionId)
                .build();
    }

    public static CarProfile create(
            CarProfileId id,
            String name,
            String vin,
            String plates,
            LocalDate serviceCertificateDate,
            LocalDate insuranceDate,
            UserId customerId,
            Integer fileId,
            Integer modelVersionId) {
        return CarProfile.builder()
                .id(id)
                .name(name)
                .vin(vin)
                .plates(plates)
                .serviceCertificateDate(validateDate(serviceCertificateDate, "serviceCertificateDate"))
                .insuranceDate(validateDate(insuranceDate, "insuranceDate"))
                .customerId(customerId)
                .fileId(fileId)
                .modelVersionId(modelVersionId)
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
