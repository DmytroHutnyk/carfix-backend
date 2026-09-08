package com.hutnyk.carfix.service;

import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class ServiceCategory {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;

    @Builder
    private ServiceCategory(Integer id, String name) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
    }

    public static ServiceCategory of(Integer id, String name) {
        return ServiceCategory.builder()
                .id(id)
                .name(name)
                .build();
    }
}
