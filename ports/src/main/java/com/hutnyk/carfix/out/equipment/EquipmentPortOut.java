package com.hutnyk.carfix.out.equipment;

import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentType;
import com.hutnyk.carfix.in.equipment.query.OwnerEquipmentView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipmentPortOut {
    EquipmentType insertType(EquipmentType type);
    Equipment insert(Equipment equipment);
    Equipment update(Equipment equipment);
    List<OwnerEquipmentView> findViewsByBranchId(UUID branchId);
    Optional<Equipment> findByIdAndBranchId(Integer equipmentId, UUID branchId);
    Optional<EquipmentType> findTypeByNameForBranch(String name, UUID branchId);
}
