package com.hutnyk.carfix.equipment;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Equipment {

    @EqualsAndHashCode.Include
    private final Integer id;
    private final String name;

    //Nullable
    private final String notes;
    private final EquipmentStatus status;
    private final Integer equipmentTypeId;
    private final BranchId branchId;

    @Builder
    private Equipment(
            Integer id,
            String name,
            String notes,
            EquipmentStatus status,
            Integer equipmentTypeId,
            BranchId branchId) {
        this.id = id;
        this.name = Validator.notBlank(name, "name");
        this.notes = notes;
        this.status = Validator.notNull(status, "status");
        this.equipmentTypeId = Validator.notNull(equipmentTypeId, "equipmentTypeId");
        this.branchId = Validator.notNull(branchId, "branchId");
    }

    public static Equipment of(
            Integer id,
            String name,
            String notes,
            EquipmentStatus status,
            Integer equipmentTypeId,
            BranchId branchId) {
        return Equipment.builder()
                .id(id)
                .name(name)
                .notes(notes)
                .status(status)
                .equipmentTypeId(equipmentTypeId)
                .branchId(branchId)
                .build();
    }
}
