package com.hutnyk.carfix.scheduling;

import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.in.scheduling.query.EmployeeCandidateView;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.serviceBay.ServiceBay;

import java.util.Collections;
import java.util.List;

/**
 * The service chain plus the branch's active resources able to take part in it: bays of a type
 * every service accepts, employees qualifying for at least one employee requirement, equipment
 * qualifying for at least one equipment requirement.
 */
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

    // Can this branch do the job at all, ignoring time? Every employee requirement must be
    // coverable by some employee (shared role) and every equipment requirement by some unit
    // (matching type). The same person may cover several requirements here — the matcher
    // enforces distinctness later. False means no slot can exist on any date.
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
