package com.hutnyk.carfix.equipment;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class EquipmentType {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;

    //Nullable — platform (seeded) types belong to no branch
    private final BranchId branchId;

    @Builder
    private EquipmentType(Integer id, String name, BranchId branchId) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
        this.branchId = branchId;
    }

    public static EquipmentType of(Integer id, String name, BranchId branchId) {
        return EquipmentType.builder()
                .id(id)
                .name(name)
                .branchId(branchId)
                .build();
    }

    public static EquipmentType create(String name, BranchId branchId) {
        return of(null, name, Validator.notNull(branchId, "branchId"));
    }
}
