package com.hutnyk.carfix.service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record UpdateServiceRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        @NotNull @Min(1) @Max(1440) Short durationMinutes,
        @NotNull @DecimalMin("0.00") @Digits(integer = 5, fraction = 2) BigDecimal price,
        @NotNull Integer categoryId,
        @NotEmpty List<@NotBlank @Size(max = 40) String> bayTypes,
        @NotEmpty List<@NotNull @Valid ServiceEmployeeRequirementRequest> employeeRequirements,
        @NotNull List<@NotNull @Valid ServiceEquipmentRequirementRequest> equipmentRequirements
) {}
