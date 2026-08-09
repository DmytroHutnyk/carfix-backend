package com.hutnyk.carfix.service.mapper;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.equipment.entity.EquipmentTypeEntity;
import com.hutnyk.carfix.role.entity.RoleEntity;
import com.hutnyk.carfix.service.EmployeeRequirement;
import com.hutnyk.carfix.service.EquipmentRequirement;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.entity.ServiceEmployeeRequirementEntity;
import com.hutnyk.carfix.service.entity.ServiceEntity;
import com.hutnyk.carfix.service.entity.ServiceEquipmentRequirementEntity;
import com.hutnyk.carfix.serviceBay.entity.ServiceBayTypeEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class ServiceMapper {

    public static Service toDomain(ServiceEntity e) {
        if (e == null) return null;
        return Service.of(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getDurationMinutes(),
                e.getPrice(),
                e.getStatus(),
                BranchId.of(e.getBranchEntity().getId()),
                e.getServiceCategoryEntity().getId(),
                e.getServiceBayTypes().stream().map(ServiceBayTypeEntity::getId).collect(Collectors.toSet()),
                e.getEmployeeRequirements().stream().map(ServiceMapper::toDomain).toList(),
                e.getEquipmentRequirements().stream().map(ServiceMapper::toDomain).toList());
    }

    public static EmployeeRequirement toDomain(ServiceEmployeeRequirementEntity e) {
        if (e == null) return null;
        Set<Integer> roleIds = e.getRoles().stream().map(RoleEntity::getId).collect(Collectors.toSet());
        return EmployeeRequirement.of(e.getId(), e.getName(), roleIds);
    }

    public static EquipmentRequirement toDomain(ServiceEquipmentRequirementEntity e) {
        if (e == null) return null;
        Set<Integer> typeIds =
                e.getEquipmentTypes().stream().map(EquipmentTypeEntity::getId).collect(Collectors.toSet());
        return EquipmentRequirement.of(e.getId(), e.getName(), typeIds);
    }
}
