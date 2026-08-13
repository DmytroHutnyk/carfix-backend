package com.hutnyk.carfix.service;

import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Set;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class EquipmentRequirement {

    @EqualsAndHashCode.Include
    private final Integer id;

    private final String name;
    private final Set<Integer> equipmentTypeIds;

    @Builder
    private EquipmentRequirement(Integer id, String name, Set<Integer> equipmentTypeIds) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
        this.equipmentTypeIds = validateEquipmentTypeIds(equipmentTypeIds);
    }

    public static EquipmentRequirement of(Integer id, String name, Set<Integer> equipmentTypeIds) {
        return EquipmentRequirement.builder()
                .id(id)
                .name(name)
                .equipmentTypeIds(equipmentTypeIds)
                .build();
    }

    private static Set<Integer> validateEquipmentTypeIds(Set<Integer> equipmentTypeIds) {
        Validator.notNull(equipmentTypeIds, "equipmentTypeIds");

        if (equipmentTypeIds.isEmpty()) {
            throw new DomainObjectValidationException(
                    ValidationErrorType.VALUE_OUT_OF_RANGE, "equipmentTypeIds", equipmentTypeIds);
        }

        return Set.copyOf(equipmentTypeIds);
    }
}
