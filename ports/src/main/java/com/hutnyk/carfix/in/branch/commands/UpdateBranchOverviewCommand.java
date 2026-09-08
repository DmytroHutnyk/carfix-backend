package com.hutnyk.carfix.in.branch.commands;

import com.hutnyk.carfix.branch.CancellationPolicy;

import java.util.List;
import java.util.Set;

public record UpdateBranchOverviewCommand(
        String name,
        String description,
        CancellationPolicy cancellationPolicy,
        RegisterBranchAddressCommand address,
        List<RegisterBranchOpeningHoursCommand> openingHours,
        List<UpdateBranchOpeningHoursExceptionCommand> openingHoursExceptions,
        Set<Integer> carBrandIds
) {
}
