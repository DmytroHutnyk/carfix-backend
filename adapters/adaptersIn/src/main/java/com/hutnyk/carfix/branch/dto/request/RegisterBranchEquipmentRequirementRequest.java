package com.hutnyk.carfix.branch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RegisterBranchEquipmentRequirementRequest(
        @NotBlank @Size(max = 100) String name,
        @NotEmpty List<@NotBlank @Size(max = 40) String> types
) {}
