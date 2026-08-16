package com.hutnyk.carfix.in.branch.commands;

import java.util.List;

public record RegisterBranchEquipmentRequirementCommand(String name, List<String> types) {
}
