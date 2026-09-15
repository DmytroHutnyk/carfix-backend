package com.hutnyk.carfix.service;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.equipment.EquipmentType;
import com.hutnyk.carfix.in.service.OwnerServicePortIn;
import com.hutnyk.carfix.in.service.commands.CreateServiceCommand;
import com.hutnyk.carfix.in.service.commands.ServiceEmployeeRequirementCommand;
import com.hutnyk.carfix.in.service.commands.ServiceEquipmentRequirementCommand;
import com.hutnyk.carfix.in.service.commands.UpdateServiceCommand;
import com.hutnyk.carfix.in.service.query.OwnerServiceView;
import com.hutnyk.carfix.in.serviceBay.query.ServiceBayTypeView;
import com.hutnyk.carfix.out.branch.OwnerBranchPortOut;
import com.hutnyk.carfix.out.equipment.EquipmentPortOut;
import com.hutnyk.carfix.out.role.RolePortOut;
import com.hutnyk.carfix.out.service.ServiceCategoryPortOut;
import com.hutnyk.carfix.out.service.ServicePortOut;
import com.hutnyk.carfix.out.serviceBay.ServiceBayPortOut;
import com.hutnyk.carfix.out.user.UserPortOut;
import com.hutnyk.carfix.role.Role;
import com.hutnyk.carfix.service.exception.ServiceCategoryNotFoundException;
import com.hutnyk.carfix.service.exception.ServiceInUseException;
import com.hutnyk.carfix.service.exception.ServiceNotFoundException;
import com.hutnyk.carfix.serviceBay.ServiceBayType;
import com.hutnyk.carfix.user.User;
import com.hutnyk.carfix.user.exception.AuthenticatedUserMissingException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@ApplicationService
public class ServiceService implements OwnerServicePortIn {

    private final ServicePortOut servicePortOut;
    private final OwnerBranchPortOut ownerBranchPortOut;
    private final UserPortOut userPortOut;
    private final ServiceCategoryPortOut serviceCategoryPortOut;
    private final ServiceBayPortOut serviceBayPortOut;
    private final RolePortOut rolePortOut;
    private final EquipmentPortOut equipmentPortOut;

    @Override
    @Transactional(readOnly = true)
    public List<OwnerServiceView> getServices(String ownerEmail, UUID branchId) {
        requireOwnedBranch(ownerEmail, branchId);
        return servicePortOut.findViewsByBranchId(branchId);
    }

    @Override
    public OwnerServiceView createService(String ownerEmail, UUID branchId, CreateServiceCommand command) {
        requireOwnedBranch(ownerEmail, branchId);
        Service created = servicePortOut.insert(buildService(
                null, ServiceStatus.ACTIVE, branchId, command.name(), command.description(),
                command.durationMinutes(), command.price(), command.categoryId(), command.bayTypes(),
                command.employeeRequirements(), command.equipmentRequirements()));
        return servicePortOut.findViewByIdAndBranchId(created.getId(), branchId)
                .orElseThrow(() -> new ServiceNotFoundException(created.getId()));
    }

    @Override
    public OwnerServiceView updateService(
            String ownerEmail, UUID branchId, Integer serviceId, UpdateServiceCommand command) {
        requireOwnedBranch(ownerEmail, branchId);
        Service existing = servicePortOut.findByIdAndBranchId(serviceId, branchId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));
        servicePortOut.update(buildService(
                existing.getId(), existing.getStatus(), branchId, command.name(), command.description(),
                command.durationMinutes(), command.price(), command.categoryId(), command.bayTypes(),
                command.employeeRequirements(), command.equipmentRequirements()));
        return servicePortOut.findViewByIdAndBranchId(serviceId, branchId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));
    }

    @Override
    public OwnerServiceView activateService(String ownerEmail, UUID branchId, Integer serviceId) {
        requireOwnedBranch(ownerEmail, branchId);
        Service existing = servicePortOut.findByIdAndBranchId(serviceId, branchId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));
        servicePortOut.updateStatus(existing.activate());
        return servicePortOut.findViewByIdAndBranchId(serviceId, branchId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));
    }

    @Override
    public OwnerServiceView suspendService(String ownerEmail, UUID branchId, Integer serviceId) {
        requireOwnedBranch(ownerEmail, branchId);
        Service existing = servicePortOut.findByIdAndBranchId(serviceId, branchId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));
        servicePortOut.updateStatus(existing.suspend());
        return servicePortOut.findViewByIdAndBranchId(serviceId, branchId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));
    }

    @Override
    public void deleteService(String ownerEmail, UUID branchId, Integer serviceId) {
        requireOwnedBranch(ownerEmail, branchId);
        servicePortOut.findByIdAndBranchId(serviceId, branchId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));
        if (servicePortOut.existsBookingReference(serviceId)) {
            throw new ServiceInUseException(serviceId);
        }
        servicePortOut.deleteById(serviceId);
    }

    private Service buildService(
            Integer id, ServiceStatus status, UUID branchId, String name, String description,
            Short durationMinutes, BigDecimal price, Integer categoryId, List<String> bayTypes,
            List<ServiceEmployeeRequirementCommand> employeeRequirements,
            List<ServiceEquipmentRequirementCommand> equipmentRequirements) {
        requireKnownCategory(categoryId);
        Map<String, Integer> bayTypeIds = bayTypeMap(branchId);
        Map<String, Integer> roleIds = roleMap(branchId);
        Map<String, Integer> equipmentTypeIds = new HashMap<>();
        Set<Integer> resolvedBayTypes = resolveBayTypes(bayTypes, bayTypeIds, branchId);
        List<EmployeeRequirement> employees = employeeRequirements.stream()
                .map(r -> EmployeeRequirement.of(null, r.name(), resolveRoles(r.roles(), roleIds, branchId)))
                .toList();
        List<EquipmentRequirement> equipment = equipmentRequirements.stream()
                .map(r -> EquipmentRequirement.of(null, r.name(),
                        resolveEquipmentTypes(r.types(), branchId, equipmentTypeIds)))
                .toList();
        return Service.of(id, name.trim(), normalizeDescription(description), durationMinutes, price, status,
                BranchId.of(branchId), categoryId, resolvedBayTypes, employees, equipment);
    }

    private void requireKnownCategory(Integer categoryId) {
        boolean known = serviceCategoryPortOut.findAll().stream()
                .anyMatch(category -> category.getId().equals(categoryId));
        if (!known) {
            throw new ServiceCategoryNotFoundException(categoryId);
        }
    }

    private Map<String, Integer> bayTypeMap(UUID branchId) {
        Map<String, Integer> map = new HashMap<>();
        for (ServiceBayTypeView type : serviceBayPortOut.findTypesForBranch(branchId)) {
            map.put(key(type.name()), type.id());
        }
        return map;
    }

    private Map<String, Integer> roleMap(UUID branchId) {
        Map<String, Integer> map = new HashMap<>();
        for (Role role : rolePortOut.findAllForBranch(branchId)) {
            map.put(key(role.getName()), role.getId());
        }
        return map;
    }

    private Set<Integer> resolveBayTypes(List<String> names, Map<String, Integer> bayTypeIds, UUID branchId) {
        Set<Integer> ids = new LinkedHashSet<>();
        for (String name : names) {
            Integer id = bayTypeIds.get(key(name));
            if (id == null) {
                id = serviceBayPortOut.insertType(ServiceBayType.create(name.trim(), BranchId.of(branchId))).getId();
                bayTypeIds.put(key(name), id);
            }
            ids.add(id);
        }
        return ids;
    }

    private Set<Integer> resolveRoles(List<String> names, Map<String, Integer> roleIds, UUID branchId) {
        Set<Integer> ids = new LinkedHashSet<>();
        for (String name : names) {
            Integer id = roleIds.get(key(name));
            if (id == null) {
                id = rolePortOut.insert(Role.create(name.trim(), BranchId.of(branchId))).getId();
                roleIds.put(key(name), id);
            }
            ids.add(id);
        }
        return ids;
    }

    private Set<Integer> resolveEquipmentTypes(List<String> names, UUID branchId, Map<String, Integer> equipmentTypeIds) {
        Set<Integer> ids = new LinkedHashSet<>();
        for (String name : names) {
            Integer id = equipmentTypeIds.get(key(name));
            if (id == null) {
                id = equipmentPortOut.findTypeByNameForBranch(name.trim(), branchId)
                        .map(EquipmentType::getId)
                        .orElseGet(() -> equipmentPortOut.insertType(
                                EquipmentType.create(name.trim(), BranchId.of(branchId))).getId());
                equipmentTypeIds.put(key(name), id);
            }
            ids.add(id);
        }
        return ids;
    }

    private void requireOwnedBranch(String ownerEmail, UUID branchId) {
        User owner = userPortOut.loadUserByEmail(ownerEmail)
                .orElseThrow(() -> AuthenticatedUserMissingException.forEmail(ownerEmail));
        ownerBranchPortOut.findDetailByIdAndOwnerId(branchId, owner.getId())
                .orElseThrow(() -> new BranchNotFoundException(branchId));
    }

    private static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    private static String key(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
