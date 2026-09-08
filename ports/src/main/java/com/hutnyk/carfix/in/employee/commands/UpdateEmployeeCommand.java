package com.hutnyk.carfix.in.employee.commands;

import java.math.BigDecimal;
import java.util.List;

public record UpdateEmployeeCommand(
        String name,
        String surname,
        String phone,
        String email,
        BigDecimal salary,
        List<String> roleNames,
        String street,
        String apartment,
        String region,
        String country,
        String postalCode
) {
}
