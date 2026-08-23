package com.hutnyk.carfix.branch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RegisterBranchEmployeeRequirementRequest(
        @NotBlank @Size(max = 100) String name,
        @NotEmpty List<@NotBlank @Size(max = 50) String> roles
) {}
