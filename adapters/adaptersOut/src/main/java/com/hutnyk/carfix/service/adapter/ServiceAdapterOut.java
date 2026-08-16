package com.hutnyk.carfix.service.adapter;

import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.equipment.entity.EquipmentTypeEntity;
import com.hutnyk.carfix.out.service.ServicePortOut;
import com.hutnyk.carfix.role.entity.RoleEntity;
import com.hutnyk.carfix.service.EmployeeRequirement;
import com.hutnyk.carfix.service.EquipmentRequirement;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.entity.ServiceCategoryEntity;
import com.hutnyk.carfix.service.entity.ServiceEmployeeRequirementEntity;
import com.hutnyk.carfix.service.entity.ServiceEntity;
import com.hutnyk.carfix.service.entity.ServiceEquipmentRequirementEntity;
import com.hutnyk.carfix.service.mapper.ServiceMapper;
import com.hutnyk.carfix.service.repository.ServiceRepository;
import com.hutnyk.carfix.serviceBay.entity.ServiceBayTypeEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@PersistenceAdapter
public class ServiceAdapterOut implements ServicePortOut {

    private final EntityManager entityManager;
    private final ServiceRepository serviceRepository;

    @Override
    public List<Service> loadByIds(Collection<Integer> serviceIds) {
        if (serviceIds.isEmpty()) return List.of();
        return serviceRepository.findAllWithRequirementsByIdIn(serviceIds).stream()
                .map(ServiceMapper::toDomain)
                .toList();
    }

    @Override
    public Service insert(Service service) {
        BranchEntity branch = entityManager.getReference(BranchEntity.class, service.getBranchId().id());
        ServiceCategoryEntity category =
                entityManager.getReference(ServiceCategoryEntity.class, service.getServiceCategoryId());
        Set<ServiceBayTypeEntity> bayTypes = service.getServiceBayTypeIds().stream()
                .map(id -> entityManager.getReference(ServiceBayTypeEntity.class, id))
                .collect(Collectors.toSet());

        ServiceEntity entity = ServiceMapper.toEntity(service, branch, category, bayTypes);
        entityManager.persist(entity);

        /* Requirement rows are the inverse side: persisted one by one after the service has its id. */
        Set<ServiceEmployeeRequirementEntity> employeeRequirements = new HashSet<>();
        for (EmployeeRequirement requirement : service.getEmployeeRequirements()) {
            Set<RoleEntity> roles = requirement.getRoleIds().stream()
                    .map(id -> entityManager.getReference(RoleEntity.class, id))
                    .collect(Collectors.toSet());
            ServiceEmployeeRequirementEntity requirementEntity = ServiceMapper.toEntity(requirement, entity, roles);
            entityManager.persist(requirementEntity);
            employeeRequirements.add(requirementEntity);
        }
        Set<ServiceEquipmentRequirementEntity> equipmentRequirements = new HashSet<>();
        for (EquipmentRequirement requirement : service.getEquipmentRequirements()) {
            Set<EquipmentTypeEntity> types = requirement.getEquipmentTypeIds().stream()
                    .map(id -> entityManager.getReference(EquipmentTypeEntity.class, id))
                    .collect(Collectors.toSet());
            ServiceEquipmentRequirementEntity requirementEntity = ServiceMapper.toEntity(requirement, entity, types);
            entityManager.persist(requirementEntity);
            equipmentRequirements.add(requirementEntity);
        }
        entity.setEmployeeRequirements(employeeRequirements);
        entity.setEquipmentRequirements(equipmentRequirements);
        entityManager.flush();
        return ServiceMapper.toDomain(entity);
    }
}
