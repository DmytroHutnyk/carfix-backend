package com.hutnyk.carfix.equipment;

import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class EquipmentType {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;

    @Builder
    private EquipmentType(Integer id, String name) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
    }

    public static EquipmentType of(Integer id, String name) {
        return EquipmentType.builder()
                .id(id)
                .name(name)
                .build();
    }
}
