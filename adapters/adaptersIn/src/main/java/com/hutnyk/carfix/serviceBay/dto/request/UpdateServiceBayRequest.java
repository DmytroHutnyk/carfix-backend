package com.hutnyk.carfix.serviceBay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateServiceBayRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 40) String serviceBayType,
        @Size(max = 2000) String notes
) {}
