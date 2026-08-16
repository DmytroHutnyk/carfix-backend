package com.hutnyk.carfix.in.branch.commands;

import java.util.List;

public record RegisterBranchEmployeeRequirementCommand(String name, List<String> roles) {
}
