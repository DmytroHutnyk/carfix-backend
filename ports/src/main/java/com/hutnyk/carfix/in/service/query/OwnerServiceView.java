package com.hutnyk.carfix.in.service.query;

import com.hutnyk.carfix.service.ServiceStatus;

import java.math.BigDecimal;
import java.util.List;

public record OwnerServiceView(
        Integer id,
        String name,
        String description,
        Short durationMinutes,
        BigDecimal price,
        ServiceStatus status,
        Integer categoryId,
        String categoryName,
        List<String> bayTypes,
        List<EmployeeRequirementView> employeeRequirements,
        List<EquipmentRequirementView> equipmentRequirements) {
}
