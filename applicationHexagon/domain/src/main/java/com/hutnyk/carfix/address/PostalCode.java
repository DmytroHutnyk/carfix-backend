package com.hutnyk.carfix.address;

import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class PostalCode {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String code;
    private final Integer cityId;

    @Builder
    private PostalCode(Integer id, String code, Integer cityId) {
        this.id = Validator.notNull(id, "id");
        this.code = Validator.notEmpty(code, "code"); //TODO add regex validation per country
        this.cityId = Validator.notNull(cityId, "cityId");
    }

    public static PostalCode of(Integer id, String code, Integer cityId){
        return PostalCode.builder()
                .id(id)
                .code(code)
                .cityId(cityId)
                .build();
    }
}
