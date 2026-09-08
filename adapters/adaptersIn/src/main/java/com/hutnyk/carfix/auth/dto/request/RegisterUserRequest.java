package com.hutnyk.carfix.auth.dto.request;

import com.hutnyk.carfix.validation.NotCommonPassword;
import com.hutnyk.carfix.validation.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
public record RegisterUserRequest(
        @NotBlank
        @Size(max = 50, message = "Name cannot exceed 50 characters")
        String name,

        @NotBlank
        @Size(max = 50, message = "Surname cannot exceed 50 characters")
        String surname,

        @NotBlank
        @Size(max = 4, message = "Country code cannot exceed 4 characters")
        @Pattern(regexp = "\\+[0-9]{1,3}")
        String phoneCountryCode,

        @NotBlank
        @Size(max = 15, message = "Phone number cannot exceed 15 digits")
        @Pattern(
                regexp = "^[0-9]{5,15}$",
                message = "Phone number must contain from 5 to 15 digits"
        )
        String phoneNumber,

        @NotBlank
        @Email
        @Size(max = 30, message = "Email cannot exceed 30 characters")
        String email,

        @Password(message = "Password must be 8–20 characters long and include an uppercase letter, a lowercase letter, a digit and a special character")
        @NotCommonPassword(message = "Password is too common")
        String password
) {
}
