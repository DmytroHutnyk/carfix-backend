package com.hutnyk.carfix.out.equipment;

import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentType;

public interface EquipmentPortOut {
    EquipmentType insertType(EquipmentType type);
    Equipment insert(Equipment equipment);
}
