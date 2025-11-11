package com.hutnyk.carfix;

import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PostalCode {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String code;
    private final Integer cityId;

    @Builder
    private PostalCode(Integer id, String code, Integer cityId) {
        this.id = Validator.notNull(id);
        this.code = Validator.notEmpty(code); //TODO add regex validation per country
        this.cityId = Validator.notNull(cityId);
    }

    public static PostalCode of(Integer id, String code, Integer cityId){
        return PostalCode.builder()
                .id(id)
                .code(code)
                .cityId(cityId)
                .build();
    }
}
