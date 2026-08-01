package com.hutnyk.carfix.dto.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateUserRequest(
        @Size(max = 50, message = "Name cannot exceed 50 characters")
        String name,

        @Size(max = 50, message = "Surname cannot exceed 50 characters")
        String surname,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth
) {
}
