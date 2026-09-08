package com.hutnyk.carfix.in.equipment.query;

import com.hutnyk.carfix.equipment.EquipmentStatus;

public record OwnerEquipmentView(
        Integer id,
        String name,
        String typeName,
        //Nullable
        String notes,
        EquipmentStatus status
) {}
