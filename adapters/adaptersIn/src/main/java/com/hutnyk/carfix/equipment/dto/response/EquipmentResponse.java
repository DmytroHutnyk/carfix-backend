package com.hutnyk.carfix.equipment.dto.response;

public record EquipmentResponse(
        int id,
        String name,
        String type,
        String notes,
        String status
) {}
