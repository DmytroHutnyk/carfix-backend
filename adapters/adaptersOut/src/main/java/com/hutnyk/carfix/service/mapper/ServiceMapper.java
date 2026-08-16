package com.hutnyk.carfix.service.mapper;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.equipment.entity.EquipmentTypeEntity;
import com.hutnyk.carfix.role.entity.RoleEntity;
import com.hutnyk.carfix.service.EmployeeRequirement;
import com.hutnyk.carfix.service.EquipmentRequirement;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.ServiceCategory;
import com.hutnyk.carfix.service.entity.ServiceCategoryEntity;
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

    public static ServiceCategory toDomain(ServiceCategoryEntity e) {
        if (e == null) return null;
        return ServiceCategory.of(e.getId(), e.getName());
    }

    public static ServiceEntity toEntity(
            Service service,
            BranchEntity branch,
            ServiceCategoryEntity category,
            Set<ServiceBayTypeEntity> bayTypes) {
        if (service == null) return null;
        ServiceEntity entity = new ServiceEntity();
        entity.setId(service.getId());
        entity.setName(service.getName());
        entity.setDescription(service.getDescription());
        entity.setDurationMinutes(service.getDurationMinutes());
        entity.setPrice(service.getPrice());
        entity.setStatus(service.getStatus());
        entity.setBranchEntity(branch);
        entity.setServiceCategoryEntity(category);
        entity.setServiceBayTypes(bayTypes);
        return entity;
    }

    public static ServiceEmployeeRequirementEntity toEntity(
            EmployeeRequirement requirement, ServiceEntity service, Set<RoleEntity> roles) {
        if (requirement == null) return null;
        ServiceEmployeeRequirementEntity entity = new ServiceEmployeeRequirementEntity();
        entity.setId(requirement.getId());
        entity.setName(requirement.getName());
        entity.setServiceEntity(service);
        entity.setRoles(roles);
        return entity;
    }

    public static ServiceEquipmentRequirementEntity toEntity(
            EquipmentRequirement requirement, ServiceEntity service, Set<EquipmentTypeEntity> types) {
        if (requirement == null) return null;
        ServiceEquipmentRequirementEntity entity = new ServiceEquipmentRequirementEntity();
        entity.setId(requirement.getId());
        entity.setName(requirement.getName());
        entity.setServiceEntity(service);
        entity.setEquipmentTypes(types);
        return entity;
    }
}
