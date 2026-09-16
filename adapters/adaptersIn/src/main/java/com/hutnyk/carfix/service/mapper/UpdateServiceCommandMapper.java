package com.hutnyk.carfix.service.mapper;

import com.hutnyk.carfix.in.service.commands.ServiceEmployeeRequirementCommand;
import com.hutnyk.carfix.in.service.commands.ServiceEquipmentRequirementCommand;
import com.hutnyk.carfix.in.service.commands.UpdateServiceCommand;
import com.hutnyk.carfix.service.dto.request.UpdateServiceRequest;

public class UpdateServiceCommandMapper {

    public static UpdateServiceCommand toCommand(UpdateServiceRequest request) {
        if (request == null) return null;
        return new UpdateServiceCommand(
                request.name(),
                request.description(),
                request.durationMinutes(),
                request.price(),
                request.categoryId(),
                request.bayTypes(),
                request.employeeRequirements().stream()
                        .map(r -> new ServiceEmployeeRequirementCommand(r.name(), r.roles()))
                        .toList(),
                request.equipmentRequirements().stream()
                        .map(r -> new ServiceEquipmentRequirementCommand(r.name(), r.types()))
                        .toList());
    }
}
