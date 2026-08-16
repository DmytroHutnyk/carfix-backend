package com.hutnyk.carfix.in.branch.commands;

import com.hutnyk.carfix.service.ServiceStatus;

import java.math.BigDecimal;
import java.util.List;

public record RegisterBranchServiceCommand(
        String name,
        String description,
        Short durationMinutes,
        BigDecimal price,
        Integer categoryId,
        ServiceStatus status,
        List<String> bayTypes,
        List<RegisterBranchEmployeeRequirementCommand> employeeRequirements,
        List<RegisterBranchEquipmentRequirementCommand> equipmentRequirements
) {
}
