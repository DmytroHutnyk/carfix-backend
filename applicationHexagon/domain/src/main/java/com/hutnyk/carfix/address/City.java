package com.hutnyk.carfix.address;

import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class City {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;
    private final Integer regionId;

    @Builder
    private City(Integer id, String name, Integer regionId) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
        this.regionId = Validator.notNull(regionId, "regionId");
    }

    public static City of(Integer id, String name, Integer regionId){
        return City.builder()
                .id(id)
                .name(name)
                .regionId(regionId)
                .build();
    }

}
