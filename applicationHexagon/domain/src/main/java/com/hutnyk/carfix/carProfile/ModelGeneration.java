package com.hutnyk.carfix.carProfile;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class ModelGeneration {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;
    private final LocalDate startProductionDate;

    //Nullable
    private final LocalDate endProductionDate;
    private final Integer carModelId;

    @Builder
    private ModelGeneration(
            Integer id,
            String name,
            LocalDate startProductionDate,
            LocalDate endProductionDate,
            Integer carModelId) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
        this.startProductionDate = validateStartProductionDate(startProductionDate, "startProductionDate");
        this.endProductionDate = validateEndProductionDate(endProductionDate, "endProductionDate");
        this.carModelId = Validator.notNull(carModelId, "carModelId");
    }

    public static ModelGeneration of(
            Integer id,
            String name,
            LocalDate startProductionDate,
            LocalDate endProductionDate,
            Integer carModelId) {
        return ModelGeneration.builder()
                .id(id)
                .name(name)
                .startProductionDate(startProductionDate)
                .endProductionDate(endProductionDate)
                .carModelId(carModelId)
                .build();
    }

    private static LocalDate validateStartProductionDate(LocalDate date, String fieldName){
        Validator.notNull(date, fieldName);

        if(date.isBefore(LocalDate.now().minusYears(100))){
            throw new DomainObjectValidationException(ValidationErrorType.DATE_TOO_OLD, fieldName, date);
        }

        return date;
    }

    private static LocalDate validateEndProductionDate(LocalDate date, String fieldName){
        if(date == null){
            return null;
        }

        if(date.isAfter(LocalDate.now().plusYears(2))){
            throw new DomainObjectValidationException(ValidationErrorType.DATE_TOO_FAR_IN_FUTURE, fieldName, date);
        }

        return date;
    }
}
