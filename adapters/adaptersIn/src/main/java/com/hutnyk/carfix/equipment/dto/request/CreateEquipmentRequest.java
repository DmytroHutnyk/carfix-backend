package com.hutnyk.carfix.equipment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEquipmentRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 50) String type,
        @Size(max = 500) String notes
) {}
