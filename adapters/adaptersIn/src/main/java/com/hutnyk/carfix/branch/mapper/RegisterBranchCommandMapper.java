package com.hutnyk.carfix.branch.mapper;

import com.hutnyk.carfix.branch.dto.request.RegisterBranchAddressRequest;
import com.hutnyk.carfix.branch.dto.request.RegisterBranchEmployeeRequest;
import com.hutnyk.carfix.branch.dto.request.RegisterBranchEmployeeRequirementRequest;
import com.hutnyk.carfix.branch.dto.request.RegisterBranchEquipmentRequest;
import com.hutnyk.carfix.branch.dto.request.RegisterBranchEquipmentRequirementRequest;
import com.hutnyk.carfix.branch.dto.request.RegisterBranchOpeningHoursRequest;
import com.hutnyk.carfix.branch.dto.request.RegisterBranchRequest;
import com.hutnyk.carfix.branch.dto.request.RegisterBranchServiceBayRequest;
import com.hutnyk.carfix.branch.dto.request.RegisterBranchServiceRequest;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchAddressCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchEmployeeCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchEmployeeRequirementCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchEquipmentCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchEquipmentRequirementCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchOpeningHoursCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchServiceBayCommand;
import com.hutnyk.carfix.in.branch.commands.RegisterBranchServiceCommand;

import java.util.List;
import java.util.Set;

public final class RegisterBranchCommandMapper {

    private RegisterBranchCommandMapper() {
    }

    public static RegisterBranchCommand toCommand(RegisterBranchRequest r) {
        if (r == null) {
            return null;
        }
        return new RegisterBranchCommand(
                r.name(),
                r.phoneNumber(),
                r.email(),
                r.timezone(),
                toCommand(r.address()),
                r.openingHours().stream().map(RegisterBranchCommandMapper::toCommand).toList(),
                Set.copyOf(r.carBrandIds()),
                List.copyOf(r.serviceBayTypes()),
                r.serviceBays().stream().map(RegisterBranchCommandMapper::toCommand).toList(),
                List.copyOf(r.equipmentTypes()),
                r.equipment().stream().map(RegisterBranchCommandMapper::toCommand).toList(),
                List.copyOf(r.roles()),
                r.employees().stream().map(RegisterBranchCommandMapper::toCommand).toList(),
                r.services().stream().map(RegisterBranchCommandMapper::toCommand).toList());
    }

    private static RegisterBranchAddressCommand toCommand(RegisterBranchAddressRequest a) {
        return new RegisterBranchAddressCommand(a.streetName(), a.buildingNumber(), a.flatNumber(), a.postalCode(),
                a.city(), a.region(), a.countryIso(), a.latitude(), a.longitude(), a.googlePlaceId());
    }

    private static RegisterBranchOpeningHoursCommand toCommand(RegisterBranchOpeningHoursRequest h) {
        return new RegisterBranchOpeningHoursCommand(h.dayOfWeek(), h.opensAt(), h.closesAt(), h.mode());
    }

    private static RegisterBranchServiceBayCommand toCommand(RegisterBranchServiceBayRequest b) {
        return new RegisterBranchServiceBayCommand(b.name(), b.type());
    }

    private static RegisterBranchEquipmentCommand toCommand(RegisterBranchEquipmentRequest e) {
        return new RegisterBranchEquipmentCommand(e.name(), e.type());
    }

    private static RegisterBranchEmployeeCommand toCommand(RegisterBranchEmployeeRequest e) {
        return new RegisterBranchEmployeeCommand(e.firstName(), e.lastName(), List.copyOf(e.roles()));
    }

    private static RegisterBranchServiceCommand toCommand(RegisterBranchServiceRequest s) {
        return new RegisterBranchServiceCommand(s.name(), s.description(), s.durationMinutes(), s.price(),
                s.categoryId(), s.status(), List.copyOf(s.bayTypes()),
                s.employeeRequirements().stream().map(RegisterBranchCommandMapper::toCommand).toList(),
                s.equipmentRequirements().stream().map(RegisterBranchCommandMapper::toCommand).toList());
    }

    private static RegisterBranchEmployeeRequirementCommand toCommand(RegisterBranchEmployeeRequirementRequest r) {
        return new RegisterBranchEmployeeRequirementCommand(r.name(), List.copyOf(r.roles()));
    }

    private static RegisterBranchEquipmentRequirementCommand toCommand(RegisterBranchEquipmentRequirementRequest r) {
        return new RegisterBranchEquipmentRequirementCommand(r.name(), List.copyOf(r.types()));
    }
}
