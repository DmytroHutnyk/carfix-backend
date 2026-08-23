package com.hutnyk.carfix.branch.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RegisterBranchRequest(

        @NotBlank @Size(max = 100)
        String name,

        @NotBlank @Pattern(regexp = "^\\+[0-9]{5,15}$", message = "Phone must be international, e.g. +48221234567")
        String phoneNumber,

        @NotBlank @Email @Size(max = 50)
        String email,

        @NotBlank @Size(max = 64)
        String timezone,

        @NotNull @Valid
        RegisterBranchAddressRequest address,

        @NotNull @Size(max = 7)
        List<@NotNull @Valid RegisterBranchOpeningHoursRequest> openingHours,

        @NotNull
        List<@NotNull Integer> carBrandIds,

        @NotNull
        List<@NotBlank @Size(max = 40) String> serviceBayTypes,

        @NotNull
        List<@NotNull @Valid RegisterBranchServiceBayRequest> serviceBays,

        @NotNull
        List<@NotBlank @Size(max = 40) String> equipmentTypes,

        @NotNull
        List<@NotNull @Valid RegisterBranchEquipmentRequest> equipment,

        @NotNull
        List<@NotBlank @Size(max = 50) String> roles,

        @NotNull
        List<@NotNull @Valid RegisterBranchEmployeeRequest> employees,

        @NotNull
        List<@NotNull @Valid RegisterBranchServiceRequest> services
) {}
