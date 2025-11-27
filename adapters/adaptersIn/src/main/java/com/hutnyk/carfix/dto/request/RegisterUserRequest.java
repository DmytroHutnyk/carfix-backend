package com.hutnyk.carfix.dto.request;

import com.hutnyk.carfix.validation.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank
        @Size(max = 50)
        String name,

        @NotBlank
        @Size(max = 50)
        String surname,

        @NotBlank
        @Size(max = 4)
        @Pattern(regexp = "\\+[0-9]{1,3}")
        String phoneCountryCode,

        @NotBlank
        @Size(max = 15)
        @Pattern(regexp = "^[0-9]{5,15}$")
        String phoneNumber,

        @NotBlank
        @Email
        @Size(max = 30)
        String email,

        @Password
        String password
) {
}
