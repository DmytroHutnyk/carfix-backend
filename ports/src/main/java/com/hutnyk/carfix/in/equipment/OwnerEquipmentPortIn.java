package com.hutnyk.carfix.in.equipment;

import com.hutnyk.carfix.in.equipment.commands.CreateEquipmentCommand;
import com.hutnyk.carfix.in.equipment.commands.UpdateEquipmentCommand;
import com.hutnyk.carfix.in.equipment.query.OwnerEquipmentView;

import java.util.List;
import java.util.UUID;

public interface OwnerEquipmentPortIn {

    List<OwnerEquipmentView> getEquipment(String email, UUID branchId);

    OwnerEquipmentView createEquipment(String email, UUID branchId, CreateEquipmentCommand command);

    OwnerEquipmentView updateEquipment(String email, UUID branchId, Integer equipmentId, UpdateEquipmentCommand command);
}
