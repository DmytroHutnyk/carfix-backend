package com.hutnyk.carfix.in.branch.commands;

import java.util.List;
import java.util.Set;

public record RegisterBranchCommand(
        String name,
        String phoneNumber,
        String email,
        String timezone,
        RegisterBranchAddressCommand address,
        List<RegisterBranchOpeningHoursCommand> openingHours,
        Set<Integer> carBrandIds,
        List<String> serviceBayTypes,
        List<RegisterBranchServiceBayCommand> serviceBays,
        List<String> equipmentTypes,
        List<RegisterBranchEquipmentCommand> equipment,
        List<String> roles,
        List<RegisterBranchEmployeeCommand> employees,
        List<RegisterBranchServiceCommand> services
) {
}
