package com.hutnyk.carfix.in.equipment.commands;

public record CreateEquipmentCommand(
        String name,
        String type,
        String notes
) {}
