package com.hutnyk.carfix.user.dto.response;

import java.time.LocalDate;

public record UserCoreResponse(
        String id,
        String name,
        String surname,
        String phoneCountryCode,
        String phoneNumber,
        String email,
        LocalDate dateOfBirth
) {
}
