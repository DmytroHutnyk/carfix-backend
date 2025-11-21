package com.hutnyk.carfix.carProfile;

import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class CarBrand {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;

    @Builder
    private CarBrand(Integer id, String name) {
        this.id = Validator.notNull(id, "id");
        this.name = Validator.notBlank(name, "name");
    }

    public static CarBrand of(Integer id, String name){
        return CarBrand.builder()
                .id(id)
                .name(name)
                .build();
    }
}
