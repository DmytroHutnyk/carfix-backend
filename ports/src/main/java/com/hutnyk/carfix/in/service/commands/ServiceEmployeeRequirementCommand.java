package com.hutnyk.carfix.in.service.commands;

import java.util.List;

public record ServiceEmployeeRequirementCommand(String name, List<String> roles) {
}
