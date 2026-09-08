package com.hutnyk.carfix.employee.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record EmployeeResponse(
        String id,
        String name,
        String surname,
        String phone,
        String email,
        BigDecimal salary,
        List<String> roles,
        EmployeeAddressResponse address,
        String status
) {}
