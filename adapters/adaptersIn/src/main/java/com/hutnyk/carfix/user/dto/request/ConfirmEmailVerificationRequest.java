package com.hutnyk.carfix.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmEmailVerificationRequest(
        @NotBlank
        @Pattern(regexp = "^[0-9]{6}$", message = "Verification code must be 6 digits")
        String code
) {
}
