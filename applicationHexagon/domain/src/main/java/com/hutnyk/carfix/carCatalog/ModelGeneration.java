package com.hutnyk.carfix.carCatalog;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class ModelGeneration {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;
    private final Short startProduction;

    //Nullable
    private final Short endProduction;
    private final Integer carModelId;

    @Builder
    private ModelGeneration(
            Integer id,
            String name,
            Short startProduction,
            Short endProduction,
            Integer carModelId) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
        this.startProduction = validateStartProduction(startProduction, "startProduction");
        this.endProduction = validateEndProduction(endProduction, "endProduction");
        this.carModelId = Validator.notNull(carModelId, "carModelId");
    }

    public static ModelGeneration of(
            Integer id,
            String name,
            Short startProduction,
            Short endProduction,
            Integer carModelId) {
        return ModelGeneration.builder()
                .id(id)
                .name(name)
                .startProduction(startProduction)
                .endProduction(endProduction)
                .carModelId(carModelId)
                .build();
    }

    private static Short validateStartProduction(Short year, String fieldName){
        Validator.notNull(year, fieldName);

        int currentYear = java.time.Year.now().getValue();
        if(year < currentYear - 100){
            throw new DomainObjectValidationException(ValidationErrorType.DATE_TOO_OLD, fieldName, year);
        }

        return year;
    }

    private static Short validateEndProduction(Short year, String fieldName){
        if(year == null){
            return null;
        }

        int currentYear = java.time.Year.now().getValue();
        if(year > currentYear + 2){
            throw new DomainObjectValidationException(ValidationErrorType.DATE_TOO_FAR_IN_FUTURE, fieldName, year);
        }

        return year;
    }
}
