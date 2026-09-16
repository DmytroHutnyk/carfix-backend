package com.hutnyk.carfix.in.service.commands;

import java.util.List;

public record ServiceEquipmentRequirementCommand(String name, List<String> types) {
}
