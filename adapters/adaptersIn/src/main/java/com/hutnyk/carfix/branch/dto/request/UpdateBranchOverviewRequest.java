package com.hutnyk.carfix.branch.dto.request;

import com.hutnyk.carfix.branch.CancellationPolicy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateBranchOverviewRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 2000) String description,
        @NotNull CancellationPolicy cancellationPolicy,
        @NotNull @Valid RegisterBranchAddressRequest address,
        @NotNull @Size(max = 7) List<@NotNull @Valid RegisterBranchOpeningHoursRequest> openingHours,
        @NotNull List<@NotNull @Valid UpdateBranchOpeningHoursExceptionRequest> openingHoursExceptions,
        @NotNull List<@NotNull Integer> carBrandIds
) {}
