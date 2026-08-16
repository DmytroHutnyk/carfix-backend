package com.hutnyk.carfix.booking.dto.request;

import com.hutnyk.carfix.scheduling.SlotCalculator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CreateBookingRequest(

        @NotNull
        UUID branchId,

        @NotNull
        UUID carProfileId,

        @NotEmpty @Size(min = 1, max = SlotCalculator.MAX_SERVICES_PER_VISIT)
        List<@NotNull Integer> serviceIds,

        @NotNull
        LocalDate date,

        @NotNull
        LocalTime startTime
) {}
