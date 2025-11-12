package com.hutnyk.carfix.carProfile;

import com.hutnyk.carfix.InvalidDomainObjectError;
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
        this.name = Validator.notEmpty(name);
        this.startProductionDate = validateStartProductionDate(startProductionDate);
        this.endProductionDate = validateEndProductionDate(endProductionDate);
        this.carModelId = Validator.notNull(carModelId);
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

    private static LocalDate validateStartProductionDate(LocalDate date){
        Validator.notNull(date);

        if(date.isBefore(LocalDate.now().minusYears(100))){
            throw new InvalidDomainObjectError("Start production date can not be older than 100 years");
        }

        return date;
    }

    private static LocalDate validateEndProductionDate(LocalDate date){
        if(date == null){
            return null;
        }

        if(date.isBefore(LocalDate.now().plusYears(20))){
            throw new InvalidDomainObjectError("End production date can not be more than 2 years into future");
        }

        return date;
    }
}
