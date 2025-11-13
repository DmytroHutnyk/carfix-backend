package com.hutnyk.carfix.address;

import com.hutnyk.carfix.util.IsoValidator;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Country {

    @EqualsAndHashCode.Include
    private final String countryIso;
    private final String name;

    @Builder
    private Country(String countryIso, String name) {
        this.countryIso = IsoValidator.validateCountryIso(countryIso, "countryIso");
        this.name = Validator.notEmpty(name, "name");
    }

    public static Country of(String countryIso, String name){
        return Country.builder()
                .countryIso(countryIso)
                .name(name)
                .build();
    }
}
