package com.hutnyk.carfix.employee.dto.request;

import jakarta.validation.constraints.Size;

public record EmployeeAddressRequest(
        @Size(max = 100) String street,
        @Size(max = 20) String apartment,
        @Size(max = 100) String region,
        @Size(max = 100) String country,
        @Size(max = 20) String postalCode
) {}
