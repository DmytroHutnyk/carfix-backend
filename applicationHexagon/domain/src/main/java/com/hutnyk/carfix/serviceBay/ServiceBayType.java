package com.hutnyk.carfix.serviceBay;

import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class ServiceBayType {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;

    @Builder
    private ServiceBayType(Integer id, String name) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
    }

    public static ServiceBayType of(Integer id, String name) {
        return ServiceBayType.builder()
                .id(id)
                .name(name)
                .build();
    }
}
