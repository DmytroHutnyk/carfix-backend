package com.hutnyk.carfix.branch.mapper;

import com.hutnyk.carfix.branch.dto.request.UpdateBranchOpeningHoursExceptionRequest;
import com.hutnyk.carfix.branch.dto.request.UpdateBranchOverviewRequest;
import com.hutnyk.carfix.in.branch.commands.UpdateBranchOpeningHoursExceptionCommand;
import com.hutnyk.carfix.in.branch.commands.UpdateBranchOverviewCommand;

import java.util.Set;

public final class UpdateBranchOverviewCommandMapper {

    private UpdateBranchOverviewCommandMapper() {
    }

    public static UpdateBranchOverviewCommand toCommand(UpdateBranchOverviewRequest r) {
        if (r == null) {
            return null;
        }
        return new UpdateBranchOverviewCommand(
                r.name(),
                r.description(),
                r.cancellationPolicy(),
                RegisterBranchCommandMapper.toCommand(r.address()),
                r.openingHours().stream().map(RegisterBranchCommandMapper::toCommand).toList(),
                r.openingHoursExceptions().stream().map(UpdateBranchOverviewCommandMapper::toCommand).toList(),
                Set.copyOf(r.carBrandIds()));
    }

    private static UpdateBranchOpeningHoursExceptionCommand toCommand(UpdateBranchOpeningHoursExceptionRequest e) {
        return new UpdateBranchOpeningHoursExceptionCommand(
                e.date(), e.opensAt(), e.closesAt(), e.isOpen(), e.reason());
    }
}
