package com.hutnyk.carfix.scheduling;

import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.in.scheduling.query.EmployeeCandidateView;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.serviceBay.ServiceBay;

import java.util.Collections;
import java.util.List;

/** Service chain paired with branch resources eligible for at least one requirement. */
public record BranchResources(List<Service> services,
                              List<ServiceBay> bays,
                              List<EmployeeCandidateView> employees,
                              List<Equipment> equipment) {

    public BranchResources {
        services = List.copyOf(services);
        bays = List.copyOf(bays);
        employees = List.copyOf(employees);
        equipment = List.copyOf(equipment);
    }

    // This coarse check ignores time and distinctness; matcher enforces both later.
    public boolean canServe() {
        return !bays.isEmpty() && staffable() && equippable();
    }

    private boolean staffable() {
        return services.stream()
                .flatMap(s -> s.getEmployeeRequirements().stream())
                .allMatch(req -> employees.stream()
                        .anyMatch(e -> !Collections.disjoint(e.roleIds(), req.getRoleIds())));
    }

    private boolean equippable() {
        return services.stream()
                .flatMap(s -> s.getEquipmentRequirements().stream())
                .allMatch(req -> equipment.stream()
                        .anyMatch(e -> req.getEquipmentTypeIds().contains(e.getEquipmentTypeId())));
    }
}
