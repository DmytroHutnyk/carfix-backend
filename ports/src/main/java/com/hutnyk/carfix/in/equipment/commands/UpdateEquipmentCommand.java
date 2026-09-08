package com.hutnyk.carfix.in.equipment.commands;

public record UpdateEquipmentCommand(
        String name,
        String type,
        String notes
) {}
