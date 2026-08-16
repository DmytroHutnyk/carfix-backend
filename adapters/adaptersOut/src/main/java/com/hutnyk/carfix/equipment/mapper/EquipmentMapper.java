package com.hutnyk.carfix.equipment.mapper;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentType;
import com.hutnyk.carfix.equipment.entity.EquipmentEntity;
import com.hutnyk.carfix.equipment.entity.EquipmentTypeEntity;

public class EquipmentMapper {

    public static Equipment toDomain(EquipmentEntity e) {
        if (e == null) return null;
        return Equipment.of(
                e.getId(),
                e.getName(),
                e.getNotes(),
                e.getStatus(),
                e.getEquipmentTypeEntity().getId(),
                BranchId.of(e.getBranchEntity().getId()));
    }

    public static EquipmentType toTypeDomain(EquipmentTypeEntity e) {
        if (e == null) return null;
        return EquipmentType.of(
                e.getId(),
                e.getName(),
                e.getBranchEntity() == null ? null : BranchId.of(e.getBranchEntity().getId()));
    }

    public static EquipmentTypeEntity toTypeEntity(EquipmentType type, BranchEntity branch) {
        if (type == null) return null;
        EquipmentTypeEntity entity = new EquipmentTypeEntity();
        entity.setId(type.getId());
        entity.setName(type.getName());
        entity.setBranchEntity(branch);
        return entity;
    }

    public static EquipmentEntity toEntity(Equipment equipment, EquipmentTypeEntity type, BranchEntity branch) {
        if (equipment == null) return null;
        EquipmentEntity entity = new EquipmentEntity();
        entity.setId(equipment.getId());
        entity.setName(equipment.getName());
        entity.setNotes(equipment.getNotes());
        entity.setStatus(equipment.getStatus());
        entity.setEquipmentTypeEntity(type);
        entity.setBranchEntity(branch);
        return entity;
    }
}
