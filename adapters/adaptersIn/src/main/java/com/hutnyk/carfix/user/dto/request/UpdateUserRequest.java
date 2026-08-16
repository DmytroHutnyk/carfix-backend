package com.hutnyk.carfix.user.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateUserRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 50, message = "Name cannot exceed 50 characters")
        String name,

        @NotBlank(message = "Surname is required")
        @Size(max = 50, message = "Surname cannot exceed 50 characters")
        String surname,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Valid
        LocationRequest preferredLocation
) {
}
