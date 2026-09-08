package com.hutnyk.carfix.equipment.mapper;

import com.hutnyk.carfix.equipment.dto.response.EquipmentResponse;
import com.hutnyk.carfix.in.equipment.query.OwnerEquipmentView;

public final class EquipmentResponseMapper {

    private EquipmentResponseMapper() {
    }

    public static EquipmentResponse toResponse(OwnerEquipmentView view) {
        if (view == null) {
            return null;
        }
        return new EquipmentResponse(
                view.id(),
                view.name(),
                view.typeName(),
                view.notes() == null ? "" : view.notes(),
                view.status().name());
    }
}
