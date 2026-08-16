package com.hutnyk.carfix.branch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterBranchServiceBayRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 40) String type
) {}
