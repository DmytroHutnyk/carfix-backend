package com.hutnyk.carfix.in.service.commands;

import java.math.BigDecimal;
import java.util.List;

public record UpdateServiceCommand(
        String name,
        String description,
        Short durationMinutes,
        BigDecimal price,
        Integer categoryId,
        List<String> bayTypes,
        List<ServiceEmployeeRequirementCommand> employeeRequirements,
        List<ServiceEquipmentRequirementCommand> equipmentRequirements) {
}
