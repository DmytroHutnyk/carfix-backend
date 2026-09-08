package com.hutnyk.carfix.branch.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateBranchOpeningHoursExceptionRequest(
        @NotNull LocalDate date,
        LocalTime opensAt,
        LocalTime closesAt,
        @NotNull Boolean isOpen,
        @Size(max = 300) String reason
) {}
