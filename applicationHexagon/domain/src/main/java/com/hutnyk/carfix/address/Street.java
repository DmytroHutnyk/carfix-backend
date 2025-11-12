package com.hutnyk.carfix.address;

import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Street {

    @EqualsAndHashCode.Include
    private final Long id;
    private final String name;
    private final Integer cityId;

    @Builder
    private Street(Long id, String name, Integer cityId) {
        this.id = Validator.notNull(id);
        this.name = Validator.notEmpty(name);
        this.cityId = Validator.notNull(cityId);
    }

    public static Street of(Long id, String name, Integer cityId){
        return Street.builder()
                .id(id)
                .name(name)
                .cityId(cityId)
                .build();
    }
}
