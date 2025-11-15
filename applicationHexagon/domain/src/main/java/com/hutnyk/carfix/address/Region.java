package com.hutnyk.carfix.address;

import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Region {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;
    private final String countryIso;

    @Builder
    private Region(Integer id, String name, String countryIso) {
        this.id = Validator.notNull(id, "id");
        this.name = Validator.notBlank(name, "name");
        this.countryIso = Validator.notBlank(countryIso, "countryIso");
    }

    public static Region of(Integer id, String name, String countryIso){
        return Region.builder()
                .id(id)
                .name(name)
                .countryIso(countryIso)
                .build();
    }
}
