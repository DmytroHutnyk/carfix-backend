package com.hutnyk.carfix.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginUserRequest(
        @NotBlank
        @Email
        @Size(max = 30, message = "Email cannot exceed 30 characters")
        String email,

        @NotBlank
        String password
) {
}
