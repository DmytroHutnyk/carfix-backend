package com.hutnyk.carfix.carProfile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCarProfileRequest(

        @NotBlank @Size(max = 100)
        String name,

        @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", message = "Invalid VIN format")
        String vin,

        @Pattern(regexp = "^(?=.{5,10}$)(?=.*[A-Z])(?=.*\\d)[A-Z0-9](?:[ -]?[A-Z0-9])+$", message = "Invalid plates format")
        String plates,

        LocalDate serviceCertificateDate,

        LocalDate insuranceDate,

        @NotNull
        Integer modelVersionId
) {}
