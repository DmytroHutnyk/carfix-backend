package com.hutnyk.carfix.carCatalog;


import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class CarModel {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;
    private final Integer carBrandId;

    @Builder
    private CarModel(Integer id, String name, Integer carBrandId) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
        this.carBrandId = Validator.notNull(carBrandId, "carBrandId");
    }

    public static CarModel of(Integer id, String name, Integer carBrandId) {
        return CarModel.builder()
                .id(id)
                .name(name)
                .carBrandId(carBrandId)
                .build();
    }


}
