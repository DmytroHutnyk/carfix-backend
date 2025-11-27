package com.hutnyk.carfix.dto.response;

import com.hutnyk.carfix.customer.CustomerStatus;
import com.hutnyk.carfix.user.UserRole;

import java.time.LocalDate;

public record RegisterCustomerResponse(
        String id,
        String name,
        String surname,
        String phoneCountryCode,
        String phoneNumber,
        String email,
        UserRole role,
        LocalDate dateOfBirth,
        CustomerStatus customerStatus
) {
}
