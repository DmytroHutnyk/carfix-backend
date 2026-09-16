package com.hutnyk.carfix.service.mapper;

import com.hutnyk.carfix.in.service.query.EmployeeRequirementView;
import com.hutnyk.carfix.in.service.query.EquipmentRequirementView;
import com.hutnyk.carfix.in.service.query.OwnerServiceView;
import com.hutnyk.carfix.service.dto.response.OwnerServiceResponse;
import com.hutnyk.carfix.service.dto.response.ServiceEmployeeRequirementResponse;
import com.hutnyk.carfix.service.dto.response.ServiceEquipmentRequirementResponse;

public class ServiceResponseMapper {

    public static OwnerServiceResponse toResponse(OwnerServiceView view) {
        if (view == null) return null;
        return new OwnerServiceResponse(
                view.id(),
                view.name(),
                view.description(),
                view.durationMinutes(),
                view.price(),
                view.status(),
                view.categoryId(),
                view.categoryName(),
                view.bayTypes(),
                view.employeeRequirements().stream().map(ServiceResponseMapper::toResponse).toList(),
                view.equipmentRequirements().stream().map(ServiceResponseMapper::toResponse).toList());
    }

    private static ServiceEmployeeRequirementResponse toResponse(EmployeeRequirementView view) {
        return new ServiceEmployeeRequirementResponse(view.name(), view.roles());
    }

    private static ServiceEquipmentRequirementResponse toResponse(EquipmentRequirementView view) {
        return new ServiceEquipmentRequirementResponse(view.name(), view.types());
    }
}
