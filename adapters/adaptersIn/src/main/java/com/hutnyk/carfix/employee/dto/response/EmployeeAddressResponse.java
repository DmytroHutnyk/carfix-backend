package com.hutnyk.carfix.employee.dto.response;

public record EmployeeAddressResponse(
        String street,
        String apartment,
        String region,
        String country,
        String postalCode
) {}
