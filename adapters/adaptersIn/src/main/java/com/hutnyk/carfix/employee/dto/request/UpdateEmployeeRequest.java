package com.hutnyk.carfix.employee.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record UpdateEmployeeRequest(
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Size(max = 50) String surname,
        @NotBlank @Size(max = 20) String phone,
        @NotBlank @Email @Size(max = 255) String email,
        @NotNull @PositiveOrZero BigDecimal salary,
        @NotEmpty List<@NotBlank String> roles,
        @NotNull @Valid EmployeeAddressRequest address
) {}
