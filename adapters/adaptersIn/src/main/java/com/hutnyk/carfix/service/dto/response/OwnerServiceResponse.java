package com.hutnyk.carfix.service.dto.response;

import com.hutnyk.carfix.service.ServiceStatus;

import java.math.BigDecimal;
import java.util.List;

public record OwnerServiceResponse(
        Integer id,
        String name,
        String description,
        Short durationMinutes,
        BigDecimal price,
        ServiceStatus status,
        Integer categoryId,
        String categoryName,
        List<String> bayTypes,
        List<ServiceEmployeeRequirementResponse> employeeRequirements,
        List<ServiceEquipmentRequirementResponse> equipmentRequirements
) {}
