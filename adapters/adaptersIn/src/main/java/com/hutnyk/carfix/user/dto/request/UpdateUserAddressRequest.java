package com.hutnyk.carfix.user.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateUserAddressRequest(
        @NotBlank(message = "Street is required")
        @Size(max = 100, message = "Street cannot exceed 100 characters")
        String streetName,

        @NotBlank(message = "Building number is required")
        @Size(max = 10, message = "Building number cannot exceed 10 characters")
        String buildingNumber,

        @Size(max = 10, message = "Flat number cannot exceed 10 characters")
        String flatNumber,

        @NotBlank(message = "Postal code is required")
        @Size(max = 10, message = "Postal code cannot exceed 10 characters")
        String postalCode,

        @NotBlank(message = "City is required")
        @Size(max = 100, message = "City cannot exceed 100 characters")
        String city,

        @NotBlank(message = "Region is required")
        @Size(max = 100, message = "Region cannot exceed 100 characters")
        String region,

        @NotBlank(message = "Country is required")
        @Pattern(regexp = "^[A-Z]{2}$", message = "Country must be a two-letter ISO code")
        String countryIso,

        @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
        BigDecimal latitude,

        @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
        BigDecimal longitude,

        @Size(max = 300, message = "Place id cannot exceed 300 characters")
        String googlePlaceId
) {
}
