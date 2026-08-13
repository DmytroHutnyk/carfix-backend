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
public final class EmployeeRequirement {

    @EqualsAndHashCode.Include
    private final Integer id;

    private final String name;
    private final Set<Integer> roleIds;

    @Builder
    private EmployeeRequirement(Integer id, String name, Set<Integer> roleIds) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
        this.roleIds = validateRoleIds(roleIds);
    }

    public static EmployeeRequirement of(Integer id, String name, Set<Integer> roleIds) {
        return EmployeeRequirement.builder()
                .id(id)
                .name(name)
                .roleIds(roleIds)
                .build();
    }

    private static Set<Integer> validateRoleIds(Set<Integer> roleIds) {
        Validator.notNull(roleIds, "roleIds");

        if (roleIds.isEmpty()) {
            throw new DomainObjectValidationException(ValidationErrorType.VALUE_OUT_OF_RANGE, "roleIds", roleIds);
        }

        return Set.copyOf(roleIds);
    }
}
