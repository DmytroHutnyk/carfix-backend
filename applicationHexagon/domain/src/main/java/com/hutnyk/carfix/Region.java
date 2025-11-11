package com.hutnyk.carfix;

import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Region {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;
    private final String countryIso;

    @Builder
    private Region(Integer id, String name, String countryIso) {
        this.id = Validator.notNull(id);
        this.name = Validator.notEmpty(name);
        this.countryIso = Validator.notEmpty(countryIso);
    }

    public static Region of(Integer id, String name, String countryIso){
        return Region.builder()
                .id(id)
                .name(name)
                .countryIso(countryIso)
                .build();
    }
}
