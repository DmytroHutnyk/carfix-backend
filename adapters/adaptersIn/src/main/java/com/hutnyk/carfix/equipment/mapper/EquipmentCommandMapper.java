package com.hutnyk.carfix.equipment.mapper;

import com.hutnyk.carfix.equipment.dto.request.CreateEquipmentRequest;
import com.hutnyk.carfix.equipment.dto.request.UpdateEquipmentRequest;
import com.hutnyk.carfix.in.equipment.commands.CreateEquipmentCommand;
import com.hutnyk.carfix.in.equipment.commands.UpdateEquipmentCommand;

public final class EquipmentCommandMapper {

    private EquipmentCommandMapper() {
    }

    public static CreateEquipmentCommand toCommand(CreateEquipmentRequest r) {
        if (r == null) {
            return null;
        }
        return new CreateEquipmentCommand(r.name(), r.type(), r.notes());
    }

    public static UpdateEquipmentCommand toCommand(UpdateEquipmentRequest r) {
        if (r == null) {
            return null;
        }
        return new UpdateEquipmentCommand(r.name(), r.type(), r.notes());
    }
}
