package com.hutnyk.carfix.serviceBay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateServiceBayRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull Integer serviceBayTypeId,
        @Size(max = 2000) String notes
) {}
