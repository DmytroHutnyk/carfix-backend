package com.hutnyk.carfix.employee;

public record EmployeeAddress(
        String street,
        String apartment,
        String region,
        String country,
        String postalCode) {
}
