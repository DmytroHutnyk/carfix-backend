package com.hutnyk.carfix.equipment.exception;

import com.hutnyk.carfix.exception.NotFoundException;

public class EquipmentTypeNotFoundException extends NotFoundException {

    public EquipmentTypeNotFoundException(String name) {
        super(EquipmentErrorCode.EQUIPMENT_TYPE_NOT_FOUND, "Equipment type", name);
    }
}
