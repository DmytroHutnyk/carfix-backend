package com.hutnyk.carfix.dto.request;

import com.hutnyk.carfix.validation.NotCommonPassword;
import com.hutnyk.carfix.validation.Password;
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
