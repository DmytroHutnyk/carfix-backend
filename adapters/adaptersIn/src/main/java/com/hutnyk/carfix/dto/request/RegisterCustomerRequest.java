package com.hutnyk.carfix.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterCustomerRequest(
        @NotBlank
        @Size(max = 50)
        String name,

        @NotBlank
        @Size(max = 50)
        String surname,

        @NotBlank
        @Size(max = 4)
        @Pattern(regexp = "\\+[0-9]{1,3}")
        String phCountryCode,

        @NotBlank
        @Size(max = 15)
        @Pattern(regexp = "\\+[0-9]+")
        String phoneNumber,

        @NotBlank
        @Email
        @Size(max = 30)
        String email,

        @NotBlank
        @Size(min = 8, max = 100)
        String password
) {

}
