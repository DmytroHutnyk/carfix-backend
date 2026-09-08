package com.hutnyk.carfix.equipment.exception;

import com.hutnyk.carfix.exception.NotFoundException;

public class EquipmentNotFoundException extends NotFoundException {

    public EquipmentNotFoundException(Integer equipmentId) {
        super(EquipmentErrorCode.EQUIPMENT_NOT_FOUND, "Equipment", equipmentId);
    }
}
