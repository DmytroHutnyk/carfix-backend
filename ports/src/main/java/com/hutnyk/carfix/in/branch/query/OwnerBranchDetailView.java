package com.hutnyk.carfix.in.branch.query;

import com.hutnyk.carfix.branch.BranchStatus;
import com.hutnyk.carfix.branch.CancellationPolicy;
import com.hutnyk.carfix.in.address.query.AddressView;

import java.util.List;
import java.util.UUID;

public record OwnerBranchDetailView(
        UUID branchId,
        String name,
        BranchStatus status,
        //Nullable
        String description,
        CancellationPolicy cancellationPolicy,
        String phoneNumber,
        String email,
        String timezone,
        AddressView address,
        List<BranchBrandView> brands,
        List<BranchOpeningHoursView> openingHours,
        List<OwnerBranchOpeningHoursExceptionView> openingHoursExceptions
) {}
