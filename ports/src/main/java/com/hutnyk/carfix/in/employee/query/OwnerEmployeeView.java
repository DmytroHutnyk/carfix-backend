package com.hutnyk.carfix.in.employee.query;

import com.hutnyk.carfix.employee.EmployeeStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OwnerEmployeeView(
        UUID id,
        String firstName,
        String lastName,
        String phone,
        String email,
        BigDecimal salary,
        EmployeeStatus status,
        String street,
        String apartment,
        String region,
        String country,
        String postalCode,
        List<String> roleNames
) {
}
