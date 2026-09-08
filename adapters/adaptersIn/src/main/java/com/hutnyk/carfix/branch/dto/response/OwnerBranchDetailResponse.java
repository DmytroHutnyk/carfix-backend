package com.hutnyk.carfix.branch.dto.response;

import com.hutnyk.carfix.branch.BranchStatus;
import com.hutnyk.carfix.branch.CancellationPolicy;

import java.util.List;
import java.util.UUID;

public record OwnerBranchDetailResponse(
        UUID branchId,
        String name,
        BranchStatus status,
        String description,
        CancellationPolicy cancellationPolicy,
        String phoneNumber,
        String email,
        String timezone,
        OwnerBranchAddressResponse address,
        List<BranchBrandResponse> brands,
        List<BranchOpeningHoursResponse> openingHours,
        List<OwnerBranchOpeningHoursExceptionResponse> openingHoursExceptions
) {}
