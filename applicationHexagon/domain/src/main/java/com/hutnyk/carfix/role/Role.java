package com.hutnyk.carfix.role;

import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Role {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;

    @Builder
    private Role(Integer id, String name) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
    }

    public static Role of(Integer id, String name) {
        return Role.builder()
                .id(id)
                .name(name)
                .build();
    }
}
