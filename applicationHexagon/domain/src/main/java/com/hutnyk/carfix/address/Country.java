package com.hutnyk.carfix.address;

import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Country {

    @EqualsAndHashCode.Include
    private final CountryIso countryIso;
    private final String name;

    @Builder
    private Country(CountryIso countryIso, String name) {
        this.countryIso = Validator.notNull(countryIso, "countryIso");
        this.name = Validator.notBlank(name, "name");
    }

    public static Country of(CountryIso countryIso, String name){
        return Country.builder()
                .countryIso(countryIso)
                .name(name)
                .build();
    }
}
