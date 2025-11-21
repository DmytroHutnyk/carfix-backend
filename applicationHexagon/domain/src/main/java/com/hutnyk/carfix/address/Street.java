package com.hutnyk.carfix.address;

import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Street {

    @EqualsAndHashCode.Include
    private final Long id;
    private final String name;
    private final Integer cityId;

    @Builder
    private Street(Long id, String name, Integer cityId) {
        this.id = Validator.notNull(id, "id");
        this.name = Validator.notBlank(name, "name");
        this.cityId = Validator.notNull(cityId, "cityId");
    }

    public static Street of(Long id, String name, Integer cityId){
        return Street.builder()
                .id(id)
                .name(name)
                .cityId(cityId)
                .build();
    }
}
